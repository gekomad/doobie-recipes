package ddl

import doobierecipes.Transactor._
import doobierecipes.Util._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import doobie.postgres.implicits._

/**
  * store a List[String] in an postgreSQL Array
  */
class SQLArrays extends AnyFunSuite with BeforeAndAfterAll {

  /**
    * CREATE TABLE person_pets (
    *  id SERIAL,
    *  name VARCHAR   NOT NULL UNIQUE,
    *  pets VARCHAR[] NOT NULL)
    */
  override def beforeAll(): Unit = dropCreateTablePersonPets().unsafeRunSync

  test("SQL Arrays") {

    import doobie.free.connection.ConnectionIO
    import doobie.implicits._

    case class Person(id: Long, name: String, pets: List[String])

    def insert(name: String, pets: List[String]): ConnectionIO[Person] =
      sql"insert into person_pets (name, pets) values ($name, $pets)".update
        .withUniqueGeneratedKeys("id", "name", "pets")

    assert(transactor.use { xa =>
      insert("Bob", List("Nixon", "Slappy")).transact(xa)
    }.unsafeRunSync == Person(1, "Bob", List("Nixon", "Slappy")))

    assert(transactor.use { xa =>
      insert("Alice", List.empty).transact(xa)
    }.unsafeRunSync == Person(2, "Alice", List.empty))

  }

}
