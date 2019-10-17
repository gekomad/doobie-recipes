# [Doobie recipes](https://tpolecat.github.io/doobie/docs/01-Introduction.html)
  
<table>      
<td align="left">        
<img src="https://cdn.rawgit.com/tpolecat/doobie/series/0.5.x/doobie_logo.svg" width="90">  
</td>      
</tr>      
</table>      
      
  
- CSV
- DDL
- Error handling
- Logging
- Parameterized queries
- Selectiong
- Array
- Statment fragments
- Transactions

  
### Database Setup

The test code assumes a local PostgreSQL server with a postgres user 'postgres' and password 'pass1'
```
cd /tmp

wget https://raw.githubusercontent.com/tpolecat/doobie/series/0.5.x/world.sql
  
sudo su postgres
  
psql -c "ALTER USER postgres  WITH PASSWORD 'pass1';"
  
psql -c 'create database world;' -U postgres
psql -d world -c "create type myenum as enum ('foo', 'bar')" -U postgres  
psql -c '\i world.sql' -d world -U postgres  
  
```

### run test
```
sbt test
```
