package ddl

import doobie.free.connection.ConnectionIO
import doobierecipes.Transactor._
import doobierecipes.Util._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import doobie.implicits._
import cats.effect.unsafe.implicits.global

/**
  * insert a record with autogenerated key and read retrive key
  */
class InsertReadKey extends AnyFunSuite with BeforeAndAfterAll {

  /**
    * CREATE TABLE person (
    * id   SERIAL,
    * name VARCHAR NOT NULL UNIQUE,
    * age  SMALLINT)
    */
  override def beforeAll(): Unit = dropCreateTablePerson().unsafeRunSync()

  test("insert and read key") {
    def insertAndReadId(name: String, age: Option[Short]): ConnectionIO[Int] =
      sql"insert into person (name, age) values ($name, $age)".update
        .withUniqueGeneratedKeys("id")

    val id = transactor.use(xa => insertAndReadId("Jack", None).transact(xa)).unsafeRunSync()
    assert(id == 1)
  }

}
