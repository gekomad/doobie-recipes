package ddl

import doobie.free.connection.ConnectionIO
import doobierecipes.Transactor._
import doobierecipes.Util._
import org.scalatest.funsuite.AnyFunSuite
import cats.implicits._
import doobie.implicits._
import doobie.util.update.Update
import org.scalatest.BeforeAndAfterAll

/**
  * performs a batch insert
  */
class Batch extends AnyFunSuite with BeforeAndAfterAll {

  /**
    * CREATE TABLE person (
    * id   SERIAL,
    * name VARCHAR NOT NULL UNIQUE,
    * age  SMALLINT)
    */
  override def beforeAll(): Unit = dropCreateTablePerson().unsafeRunSync()

  test("batch") {

    type PersonInfo = (String, Option[Short])

    def insertMany(ps: List[PersonInfo]): ConnectionIO[Int] =
      Update[PersonInfo]("insert into person (name, age) values (?, ?)").updateMany(ps)

    val data = List[PersonInfo](("Frank", Some(12)), ("Daddy", None))

    transactor.use(xa => insertMany(data).transact(xa)).unsafeRunSync()
  }
}
