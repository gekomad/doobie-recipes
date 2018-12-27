import cats.data.NonEmptyList
import com.github.gekomad.ittocsv.parser.IttoCSVFormat
import com.github.gekomad.ittocsv.core.FromCsv
import com.github.gekomad.ittocsv.core.FromCsv.Schema
import com.github.gekomad.ittocsv.core.Header.FieldNames
import doobie.free.connection.ConnectionIO
import org.scalatest.FunSuite

import scala.collection.immutable
import scala.concurrent.ExecutionContextExecutorService

class DDL extends FunSuite {

  import MyPredef.{createTablePerson, transactor}
  import cats.implicits._
  import doobie.implicits._

  case class Person(id: Long, name: String, age: Option[Short])

  test("insert read update") {

    //create table
    assert(createTablePerson == 0)

    //insert
    import doobie.util.update.Update0
    def insert1(name: String, age: Option[Short]): Update0 = sql"insert into person (name, age) values ($name, $age)".update

    assert(transactor.use { xa => insert1("Alice", Some(12)).run.transact(xa) }.unsafeRunSync == 1)
    assert(transactor.use { xa => insert1("Bob", None).run.transact(xa) }.unsafeRunSync == 1)

    //read
    {
      val mySelect: immutable.Seq[Person] = transactor.use { xa =>
        sql"select id, name, age from person order by name"
          .query[Person]
          .to[List] // ConnectionIO[List[Country]]
          .transact(xa) // IO[List[Country]]
      }.unsafeRunSync // List[Country]]

      assert(mySelect == List(Person(1, "Alice", Some(12)), Person(2, "Bob", None)))
    }

  }

  test("insert and read class Person") {
    //create table
    assert(createTablePerson == 0)

    def insertAndRead(name: String, age: Option[Short]): ConnectionIO[Person] = {
      sql"insert into person (name, age) values ($name, $age)"
        .update
        .withUniqueGeneratedKeys("id", "name", "age")
    }

    val elvis = transactor.use { xa => insertAndRead("Elvis", None).transact(xa) }.unsafeRunSync

    assert(elvis == Person(1, "Elvis", None))
  }

  test("insert and read id") {
    //create table
    assert(createTablePerson == 0)

    def insertAndReadId(name: String, age: Option[Short]): ConnectionIO[Int] = {
      sql"insert into person (name, age) values ($name, $age)"
        .update
        .withUniqueGeneratedKeys("id")
    }

    val id = transactor.use { xa => insertAndReadId("Jack", None).transact(xa) }.unsafeRunSync

    assert(id == 1)
  }


  test("batch") {
    //create table
    assert(createTablePerson == 0)

    import doobie.util.update.Update
    type PersonInfo = (String, Option[Short])

    def insertMany(ps: List[PersonInfo]): ConnectionIO[Int] = {
      val sql = "insert into person (name, age) values (?, ?)"
      Update[PersonInfo](sql).updateMany(ps)
    }

    // Some rows to insert
    val data = List[PersonInfo](
      ("Frank", Some(12)),
      ("Daddy", None))

    assert(transactor.use { xa => insertMany(data).transact(xa) }.unsafeRunSync == 2)

  }

}


