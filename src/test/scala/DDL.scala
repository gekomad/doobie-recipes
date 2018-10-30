import org.scalatest.FunSuite
import doobie.free.connection.ConnectionIO

import scala.collection.immutable

class DDL extends FunSuite {

  import MyPredef.transactor
  import MyPredef.createTablePerson
  import doobie.implicits._
  import cats.implicits._

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

    //    val y = xa.yolo TODO
    //    import y._
    //
    //    //update
    //    {
    //
    //      sql"update person set age = 15 where name = 'Alice'".update.quick.unsafeRunSync
    //
    //      val mySelect: immutable.Seq[Person] = transactor.use { xa =>
    //        sql"select id, name, age from person order by name"
    //          .query[Person]
    //          .to[List] // ConnectionIO[List[Country]]
    //          .transact(xa) // IO[List[Country]]
    //      }.unsafeRunSync // List[Country]]
    //
    //      assert(mySelect == List(Person(1, "Alice", Some(15)), Person(2, "Bob", None)))
    //    }
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
  /*
    test("batch and return List[Person]") { TODO

      //create table
      assert(createTablePerson == 0)
      import doobie.util.update.Update
      type PersonInfo = (String, Option[Short])

      import fs2.Stream

      def insertMany2(ps: List[PersonInfo]): Stream[ConnectionIO, Person] = {
        val sql = "insert into person (name, age) values (?, ?)"
        Update[PersonInfo](sql).updateManyWithGeneratedKeys[Person]("id", "name", "age")(ps)
      }

      // Some rows to insert
      val data = List[PersonInfo](
        ("Banjo", Some(39)),
        ("Skeeter", None),
        ("Jim-Bob", Some(12)))

      val x=transactor.use { xa =>
        insertMany2(data).compile.toList.transact(xa)
      }

      assert(x.unsafeRunSync() == List(Person(1, "Banjo", Some(39)), Person(2, "Skeeter", None), Person(3, "Jim-Bob", Some(12))))

    }
  */
}


