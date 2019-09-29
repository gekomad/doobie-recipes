import doobie.free.connection.ConnectionIO
import org.scalatest.funsuite.AnyFunSuite
import Util._

class ErrorHandling extends AnyFunSuite {

  case class Person(id: Int, name: String)

  test("error Handling") {

    //create table
    assert(createTablePerson == 0)

    import doobie.implicits._

    def insert(s: String): ConnectionIO[Person] = {
      sql"insert into person (name) values ($s)".update
        .withUniqueGeneratedKeys("id", "name")
    }

    def safeInsert(s: String) =
      insert(s).attemptSomeSqlState {
        case a => a
      }

    val res = transactor.use { xa =>
      safeInsert("bob").transact(xa)
    }.unsafeRunSync

    res match {
      case Right(r) => assert(r == Person(1, "bob"))
      case _        => assert(false)
    }

    assert(
      transactor
        .use { xa =>
          safeInsert("bob").transact(xa)
        }
        .unsafeRunSync
        .isLeft
    )

  }

}
