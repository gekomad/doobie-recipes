
import doobie.free.connection.ConnectionIO
import org.scalatest.FunSuite
import MyPredef.createTable

class ErrorHandling extends FunSuite {

  case class Person(id: Int, name: String)

  import MyPredef.xa

  test("select 1 column") {

    //create table
    assert(createTable == 0)

    import doobie.implicits._

    def insert(s: String): ConnectionIO[Person] = {
      sql"insert into person (name) values ($s)"
        .update
        .withUniqueGeneratedKeys("id", "name")
    }

    def safeInsert(s: String) =
      insert(s).attemptSomeSqlState {
        case a => a
      }

    val res = safeInsert("bob").transact(xa).unsafeRunSync
    assert(res.isRight && res.getOrElse(???) == Person(1, "bob"))

    assert(safeInsert("bob").transact(xa).unsafeRunSync.isLeft)

  }

}


