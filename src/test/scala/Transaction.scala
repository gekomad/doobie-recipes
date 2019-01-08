import doobie.enum.SqlState
import org.scalatest.FunSuite

class Transaction extends FunSuite {

  test("transaction") {
    import MyPredef.transactor
    import doobie.implicits._

    case class City(id: Long, name: String, countrycode: String, district: String, population: Int)

    import doobie.util.update.Update0

    def insertCity(id: Long, name: String, countrycode: String, district: String, population: Int): Update0 =
      sql"insert into city (id, name, countrycode,district,population) values ($id, $name, $countrycode,$district,$population)".update

    //duplicate key value violates unique constraint "city_pkey". No records insert!
    val o = transactor
      .use { xa =>
        for {
          _ <- insertCity(5000, "city1", "c1", "d1", 10).run.transact(xa).attemptSomeSqlState {
            case a => a
          }
          b <- insertCity(5000, "city1", "c1", "d1", 10).run.transact(xa).attemptSomeSqlState {
            case a => a
          }
        } yield b
      }
      .unsafeRunSync()

    o match {
      case Right(_) => assert(false)
      case Left(l)  => assert(l == SqlState("23505")) //unique constraint
    }

    //read - no records found
    {
      val mySelect = transactor.use { xa =>
        sql"select id from person where id = 5000 "
          .query[Int]
          .to[List]
          .transact(xa)
      }.unsafeRunSync

      assert(mySelect.isEmpty)
    }

  }

}
