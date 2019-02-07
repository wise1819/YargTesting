package sql;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.AbandonedObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;

import com.haulmont.yarg.exception.InitializationException;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;

public class FromSql {

	public static void main(String[] args) throws IOException, SQLException {
		ReportBuilder reportBuilder = new ReportBuilder();
		ReportTemplateBuilder reportTemplateBuilder = new ReportTemplateBuilder()
				.documentPath("resources/companySQL.docx").documentName("invoice.docx")
				.outputType(ReportOutputType.docx).readFileFromPath();
		reportBuilder.template(reportTemplateBuilder.build());
		BandBuilder bandBuilder = new BandBuilder();

		String driver = "org.postgresql.Driver";
		String dbUrl = "jdbc:postgresql://127.0.0.1:5432/TestYarg";
		String user = "postgres";
		String password = "examplePassword";

		DataSource sqlDataSource = setupDataSource(driver, dbUrl, user, password, 10, 10, 0);
		setupTable(sqlDataSource); // Database TestYarg has to exist beforehand
		// System.out.println(ds.getConnection());
		SqlDataLoader sqlDataLoader = new SqlDataLoader(sqlDataSource);
		// System.out.println(ds.getConnection());

		Reporting reporting = new Reporting();
		reporting.setFormatterFactory(new DefaultFormatterFactory());
		reporting.setLoaderFactory(new DefaultLoaderFactory().setSqlDataLoader(sqlDataLoader)
				.setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl())));

		ReportBand main = bandForHeaderAndFooter(bandBuilder); // taken from old example

		ReportBand companies = bandBuilder.name("Companies").query("Companies", "SELECT * FROM company", "sql").build();

		reportBuilder.band(companies);
		reportBuilder.band(main);
		reportBuilder.format(new ReportFieldFormatImpl("Main.signature", "${html}"));
		reportBuilder.format(new ReportFieldFormatImpl("Main.footer", "${html}"));
		Report report = reportBuilder.build();
		ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report),
				new FileOutputStream("resources/SQL-Example-raw.docx"));

	}

	private static ReportBand bandForHeaderAndFooter(BandBuilder bandBuilder) {
		ReportBand main = bandBuilder.name("Main").query("Main", "return [\n" + "                              [\n"
				+ "                               'invoiceNumber':99987,\n"
				+ "                               'client' : 'Google Inc.',\n"
				+ "                               'date' : new Date(),\n"
				+ "                               'addLine1': '1600 Amphitheatre Pkwy',\n"
				+ "                               'addLine2': 'Mountain View, USA',\n"
				+ "                               'addLine3':'CA 94043',\n"
				+ "                               'signature': '<html><body><span style=\"color:red\">Mr. Yarg</span></body></html>',\n"
				+ "                               'footer' : '<html><body><b><span style=\"color:green;font-weight:bold;\">The invoice footer</span></b></body></html>' \n"
				+ "                            ]]", "groovy").build();
		return main;
	}

	private static DataSource setupDataSource(String driver, String connectURI, String username, String password,
			Integer maxActive, Integer maxIdle, Integer maxWait) {
		try {
			Class.forName(driver);
			final AbandonedConfig config = new AbandonedConfig();
			config.setLogAbandoned(true);

			AbandonedObjectPool connectionPool = new AbandonedObjectPool(null, config);

			connectionPool.setMaxIdle(maxIdle);
			connectionPool.setMaxActive(maxActive);
			if (maxWait != null) {
				connectionPool.setMaxWait(maxWait);
			}

			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, username, password);

			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,
					connectionPool, null, null, false, true);

			connectionPool.setFactory(poolableConnectionFactory);
			return new PoolingDataSource(connectionPool);
		} catch (ClassNotFoundException e) {
			throw new InitializationException("An error occurred during creation of new datasource object", e);
		}
	}

	private static void setupTable(DataSource ds) throws SQLException {
		ds.getConnection().createStatement().executeUpdate("Drop schema public cascade");
		ds.getConnection().createStatement().executeUpdate("create schema public;");
		String sql = "CREATE TABLE COMPANY " + "(ID INT PRIMARY KEY     NOT NULL,"
				+ " NAME           TEXT    NOT NULL, " + " AGE            INT     NOT NULL, "
				+ " ADDRESS        CHAR(50), " + " SALARY         REAL)";
		ds.getConnection().createStatement().executeUpdate(sql);
		Statement stmt = ds.getConnection().createStatement();

		sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " + "VALUES (1, 'Paul', 32, 'California', 20000.00 );";
		stmt.executeUpdate(sql);

		sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " + "VALUES (2, 'Allen', 25, 'Texas', 15000.00 );";
		stmt.executeUpdate(sql);

		sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " + "VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );";
		stmt.executeUpdate(sql);

		sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " + "VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );";
		stmt.executeUpdate(sql);
	}

}
