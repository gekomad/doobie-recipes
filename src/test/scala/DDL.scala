import doobie.free.connection.ConnectionIO
import org.scalatest.FunSuite

import scala.collection.immutable
import scala.concurrent.ExecutionContextExecutorService

class DDL extends FunSuite {

  import MyPredef.{createTablePerson, transactor}
  import cats.implicits._
  import doobie.implicits._

  case class Person(id: Long, name: String, age: Option[Short])

  case class Test2(field1: Int, field2: String)

  test("read csv and insert in table") {

    import java.nio.file.Paths
    import java.util.concurrent.Executors
    import cats.effect.IO
    import fs2.{io, text}
    import scala.concurrent.ExecutionContext
    val blockingExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
    import cats.effect.ContextShift
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
    import _root_.io.chrisdavenport.cormorant._
    import _root_.io.chrisdavenport.cormorant.generic.semiauto._
    import _root_.io.chrisdavenport.cormorant.implicits._
    import cats.implicits._
    import doobie.implicits._

    val nRecords = 1005
    val inOutFile = s"${MyPredef.tmpDir}/test2.csv"

    //create csv file and table
    {

      //create csv file
      (fs2.Stream.emits(List("field1,field2")) ++ fs2.Stream.emits(List("1,bbbb")).repeat.take(nRecords))
        .intersperse("\n")
        .through(text.utf8Encode)
        .through(io.file.writeAll[IO](Paths.get(inOutFile), blockingExecutionContext))
        .compile.drain
        .unsafeRunSync()

      //create table
      assert(0 == {
        val drop = sql"""DROP TABLE IF EXISTS test2""".update.run

        val create =
          sql"""
        CREATE TABLE test2 (
        field1 SMALLINT NOT NULL,
        field2 VARCHAR NOT NULL
        )
      """.update.run

        transactor.use { xa =>
          (drop, create).mapN(_ + _).transact(xa)
        }.unsafeRunSync
      })

    }

    implicit val lrs: Read[Test2] = deriveRead

    ReadCsvAndWriteDB.readCsvAndWriteDB[Test2](inOutFile, "insert into test2 (field1, field2) values (?, ?)")

    def getTest2Count: Int = transactor.use { xa =>
      sql"select count(1) from test2"
        .query[Int].unique
        .transact(xa)
    }.unsafeRunSync()

    assert(getTest2Count == nRecords)
  }

  object ReadCsvAndWriteDB {

    import java.nio.file.Paths
    import java.util.concurrent.Executors

    import cats.effect.IO
    import fs2.{io, text}

    import scala.concurrent.ExecutionContext

    val blockingExecutionContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

    import cats.effect.ContextShift

    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

    import _root_.io.chrisdavenport.cormorant._

    import _root_.io.chrisdavenport.cormorant.implicits._
    import _root_.io.chrisdavenport.cormorant.parser._
    import cats.implicits._
    import doobie.implicits._
    import doobie.util.update.Update

    val maxRowsToCommit = 100

    //read from file N rows and store in db
    def readCsvAndWriteDB[B: doobie.util.Read : doobie.util.Write : _root_.io.chrisdavenport.cormorant.Read](inOutFile: String, sql: String) = {

      def bulkInsert[A: doobie.util.Read : doobie.util.Write : _root_.io.chrisdavenport.cormorant.Read](csvList: List[String], count: Long): Either[Error, IO[Int]] = {
        val csv = if (count == 0)
          csvList.drop(1).mkString("\n") else csvList.mkString("\n")

        //write the list into db
        val oo: Either[_root_.io.chrisdavenport.cormorant.Error, List[A]] = parseRows(csv).leftWiden[Error].flatMap(_.readRows[A].sequence)

        for {
          ll <- oo
        } yield transactor.use { xa => Update[A](sql).updateMany(ll).transact(xa) }

      }

      io.file.readAll[IO](Paths.get(inOutFile), blockingExecutionContext, 4096)
        .through(text.utf8Decode)
        .through(text.lines)
        .chunkN(maxRowsToCommit).zipWithIndex
        .map(chunk => bulkInsert[B](chunk._1.toList, chunk._2).map(_.unsafeRunSync)).compile.drain
        .unsafeRunSync()
    }
  }

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


