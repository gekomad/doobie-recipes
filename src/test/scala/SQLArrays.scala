import MyPredef.transactor
import org.scalatest.FunSuite

class SQLArrays extends FunSuite {

  import doobie.postgres.implicits._
  import MyPredef.createTablePersonPets

  test("SQL Arrays") {

    import doobie.free.connection.ConnectionIO
    import doobie.implicits._

    //create table
    assert(createTablePersonPets == 0)
    case class Person(id: Long, name: String, pets: List[String])


    def insert(name: String, pets: List[String]): ConnectionIO[Person] = {
      sql"insert into person_pets (name, pets) values ($name, $pets)"
        .update
        .withUniqueGeneratedKeys("id", "name", "pets")
    }

    assert( transactor.use { xa =>insert("Bob", List("Nixon", "Slappy")).transact(xa)}.unsafeRunSync == Person(1, "Bob", List("Nixon", "Slappy")))
    assert( transactor.use { xa =>insert("Alice", List.empty).transact(xa)}.unsafeRunSync == Person(2, "Alice", List.empty))

  }

}


