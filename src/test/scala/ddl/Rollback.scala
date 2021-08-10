package ddl

import doobie.free.connection.ConnectionIO
import doobierecipes.Transactor._
import doobierecipes.Util._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import doobie.implicits._
import cats.effect.unsafe.implicits.global

class Rollback extends AnyFunSuite with BeforeAndAfterAll {

  /**
    * CREATE TABLE person (
    * id   SERIAL,
    * name VARCHAR NOT NULL UNIQUE,
    * age  SMALLINT)
    */
  override def beforeAll(): Unit = dropCreateTablePerson().unsafeRunSync()

  test("rollback") {
    import doobie.util.transactor.Transactor
    import doobie.HC

    def insert(name: String, age: Option[Short]): ConnectionIO[Int] =
      sql"insert into person (id, name, age) values (10, $name, $age)".update.run

    def read(id: Int) =
      sql"select name from person where id = $id"
        .query[Option[String]]
        .option

    transactor
      .use { xa =>
        val xa2 = Transactor.after.set(xa, HC.rollback)
        insert("Bob", None).transact(xa2)
      }
      .unsafeRunSync()

    val name = transactor
      .use { xa =>
        read(10).transact(xa)
      }
      .unsafeRunSync()
    assert(name == None)

  }
}
