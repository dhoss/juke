# Setup

1. install java 23 (easiest way is to use [sdkman](https://sdkman.io/))
2. install/download [flyway](https://www.red-gate.com/products/flyway/community/)
3. install postgres 15
4. from the root directory of this project, run `psql -Upostgres < src/main/resources/db/setup-database.sql`
5. once that's complete, run `./flyway-local.sh migrate`
6. if there are no issues there, start with app with `mvn clean spring-boot:run` and navigate to [http://localhost:8080](http://localhost:8080/)