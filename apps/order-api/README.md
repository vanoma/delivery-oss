# Vanoma Order API

## How To Run Tests

`mvn clean test`

## How To Run Docker Container

`bash docker.sh`

## Liquibase Migrations

#### Coordinates for Address type

```txt
    <property name="pointType" value="geometry(point)" dbms="h2"/> <!--  For in-memory integration tests-->
    <property name="pointType" value="POINT" dbms="mysql, oracle, mssql, mariadb, postgresql"/>
```

```txt
 <column name="coordinates" type="${pointType}"/>
```

## Logging & Alerting

Consider Datalog & PagerDuty (recommenced by Erik at Cobalt Robots)

## Type for Money (BigDecimal)

https://stackoverflow.com/a/37217646/3112373