# Doobie recipes v.0.13 and v.1
  
<table>      
<td align="left">  
<a href="https://tpolecat.github.io/doobie/docs/01-Introduction.html">      
    <img src="https://cdn.rawgit.com/tpolecat/doobie/series/0.5.x/doobie_logo.svg" width="90">
</a>  
</td>      
</table>

### Run test on local PostgreSQL

```
cd doobie_1 or doobie_0.13
$ curl -O https://raw.githubusercontent.com/tpolecat/doobie/series/0.7.x/world.sql
$ psql -c 'create user postgres createdb'
$ psql -c 'create database world;' -U postgres
$ psql -c '\i world.sql' -d world -U postgres
$ psql -d world -c "create type myenum as enum ('foo', 'bar')" -U postgres

sbt test
```

### Run test with docker
```
cd doobie_1 or doobie_0.13
docker run -d --name doobie_recipies -p5435:5432 -e POSTGRES_USER=postgres -e POSTGRES_DB=world -e POSTGRES_PASSWORD=postgres tpolecat/skunk-world
sbt test
docker rm -f doobie_recipies

```

| v.1                                                                                                                                          | v.0.13                                                                                                                                          |
|----------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| Selecting                                                                                                                                    |
| [select count](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/Count.scala)                          | [select count](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/Count.scala)                          |
| [join](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/Join.scala)                                   | [join](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/Join.scala)                                   |
| [MappingRows](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/MappingRows.scala)                     | [MappingRows](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/MappingRows.scala)                     |
| [NestedClass](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/NestedClass.scala)                     | [NestedClass](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/NestedClass.scala)                     |
| [NestedClassMap](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/NestedClassMap.scala)               | [NestedClassMap](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/NestedClassMap.scala)                  |[NestedClassMap](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/NestedClassMap.scala)|d|
| [RowMappings](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/RowMappings.scala)                     | [RowMappings](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/RowMappings.scala)                     |
| [SelectMultipleColumns](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/SelectMultipleColumns.scala) | [SelectMultipleColumns](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/SelectMultipleColumns.scala) |
| [SelectOneColumn](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/SelectOneColumn.scala)             | [SelectOneColumn](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/SelectOneColumn.scala)             |
| [ShapelessRecord](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/ShapelessRecord.scala)             | [ShapelessRecord](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/ShapelessRecord.scala)             |
| [Streaming](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/Streaming.scala)                         | [Streaming](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/Streaming.scala)                         |
| [StatementFragments](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/StatementFragments.scala)       | [StatementFragments](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/StatementFragments.scala)       |
| [Timestamp](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/selecting/Timestamp.scala)                         | [Timestamp](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/selecting/Timestamp.scala)                         |
| Parameterized queries                                                                                                                        | Parameterized queries                                                                                                                           |
| [Bigger than](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/parameterizedQueries/BiggerThan.scala)           | [Bigger than](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/parameterizedQueries/BiggerThan.scala)           |
| [IN clauses](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/parameterizedQueries/INClauses.scala)             | [IN clauses](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/parameterizedQueries/INClauses.scala)             |
| [Parameters](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/parameterizedQueries/Parameters.scala)            | [Parameters](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/parameterizedQueries/Parameters.scala)            |
| DDL                                                                                                                                          | DDL                                                                                                                                             |
| [Batch](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/ddl/Batch.scala)                                       | [Batch](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/ddl/Batch.scala)                                       |
| [Insert and Read key](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/ddl/InsertReadKey.scala)                 | [Insert and Read key](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/ddl/InsertReadKey.scala)                 |
| [Insert and Read Person class](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/ddl/InsertReadPerson.scala)     | [Insert and Read Person class](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/ddl/InsertReadPerson.scala)     |
| [Insert Read and Update](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/ddl/InsertReadUpdate.scala)           | [Insert Read and Update](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/ddl/InsertReadUpdate.scala)           |
| [SQLArrays](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/ddl/SQLArrays.scala)                               | [SQLArrays](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/ddl/SQLArrays.scala)                               |
| [Rollback](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/ddl/Rollback.scala)                                 | [Rollback](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/ddl/Rollback.scala)                                 |
| [Vacuum](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/ddl/Vacuum.scala)                                     | [Vacuum](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/ddl/Vacuum.scala)                                     |
| Transactions                                                                                                                                 | Transactions                                                                                                                                    |
| [Transaction](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/Transaction.scala)                               | [Transaction](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/Transaction.scala)                               |
| Enum                                                                                                                                         | Enum                                                                                                                                            |
| [Enum](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/Enum.scala)                                             | [Enum](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/Enum.scala)                                             |
| CSV                                                                                                                                          | CSV                                                                                                                                             |
| [Select with type](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/csv/GenericSelect.scala)                    | [Select with type](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/csv/GenericSelect.scala)                    |
| [Itto CSV](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/csv/IttoCSV.scala)                                  | [Itto CSV](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/csv/IttoCSV.scala)                                  |
| [Load CSV in table](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/csv/LoadCSV.scala)                         | [Load CSV in table](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/csv/LoadCSV.scala)                         |
| [Spool CSV](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/csv/SpoolCSV.scala)                                | [Spool CSV](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/csv/SpoolCSV.scala)                                |
| [Spool paramterized CSV](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/csv/SpoolParameterized.scala)         | [Spool paramterized CSV](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/csv/SpoolParameterized.scala)         |
| Logging                                                                                                                                      | Logging                                                                                                                                         |
| [Logging](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/Logging.scala)                                       | [Logging](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/Logging.scala)                                       |
| Error handling                                                                                                                               | Error handling                                                                                                                                  |
| [ErrorHandling](https://github.com/gekomad/doobie-recipes/blob/master/doobie_1/src/test/scala/ErrorHandling.scala)                           | [ErrorHandling](https://github.com/gekomad/doobie-recipes/blob/master/doobie_0.13/src/test/scala/ErrorHandling.scala)                           |
                                                                                                                                                                  
