# Yarg Testing

Implement a few test for yarg here.

### Preconditions

Have a working installation of Java 8 installed

### Start hacking

Import into the IDE of your choosing as a gradle project.

Suggestions: eclipse, IntelliJ (free Ultimate Edition for Students available)

### Connection to Database

There is a possibility to connect to a postgreSQL-DB. In `resources/postgres/`
is a file `postgres-stack.yml`

Start the docker stack by
```
cd resources/postgres
docker-compose -f postgres-stack.yml -d
```

This will start a postgreSQL service and an adminer. To get started you need to
1. Open [Adminer](http://127.0.0.1:8080)
2. Log in with user 'postgres' and password 'examplePassword' choosing postgreSQL
3. create a database TestYarg
