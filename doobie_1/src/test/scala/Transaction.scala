import cats.effect.{IO, Resource}
import cats.free.Free
import doobie.free.connection
import doobie.hikari.HikariTransactor
import org.scalatest.funsuite.AnyFunSuite
import doobierecipes.Transactor._
import cats.effect.unsafe.implicits.global

class Transaction extends AnyFunSuite {

  test("transaction ko") {

    import doobie.implicits._

    case class City(id: Long, name: String, countrycode: String, district: String, population: Int)

    import doobie.util.update.Update0

    def insertCity(id: Long, name: String, countrycode: String, district: String, population: Int): Update0 =
      sql"insert into city (id, name, countrycode,district,population) values ($id, $name, $countrycode,$district,$population)".update

    //duplicate key value violates unique constraint "city_pkey". No records insert!
    val oo: Resource[IO, HikariTransactor[IO]] = transactor

    val randKey = scala.util.Random.nextInt()
    val rows: Free[connection.ConnectionOp, String] = for {
      _ <- insertCity(randKey, "city1", "c1", "d1", 10).run
      _ <- insertCity(randKey, "city1", "c1", "d1", 10).run
    } yield "inserted"

    val res = oo
      .use { xa =>
        rows.transact(xa).handleErrorWith { e =>
          IO(e.getMessage)
        }
      }
      .unsafeRunSync()

    assert(res == s"""ERROR: duplicate key value violates unique constraint "city_pkey"
                    |  Detail: Key (id)=($randKey) already exists.""".stripMargin)

    //read - no records found
    {
      val mySelect = transactor
        .use { xa =>
          fr"""select id from city where id = $randKey"""
            .query[Int]
            .to[List]
            .transact(xa)
        }
        .unsafeRunSync()
      assert(mySelect.isEmpty)
    }

  }

  test("transaction ok") {

    import doobie.implicits._

    case class City(id: Long, name: String, countrycode: String, district: String, population: Int)

    import doobie.util.update.Update0

    def insertCity(id: Long, name: String, countrycode: String, district: String, population: Int): Update0 =
      sql"insert into city (id, name, countrycode,district,population) values ($id, $name, $countrycode,$district,$population)".update

    //duplicate key value violates unique constraint "city_pkey". No records insert!
    val oo: Resource[IO, HikariTransactor[IO]] = transactor

    val randKey1 = scala.util.Random.nextInt()
    val randKey2 = scala.util.Random.nextInt()
    val rows: Free[connection.ConnectionOp, String] = for {
      _ <- insertCity(randKey1, "city1", "c1", "d1", 10).run
      _ <- insertCity(randKey2, "city1", "c1", "d1", 10).run
    } yield "inserted"

    val res = oo
      .use { xa =>
        rows.transact(xa).handleErrorWith { e =>
          IO(e.getMessage)
        }
      }
      .unsafeRunSync()

    assert(res == """inserted""".stripMargin)

    //read - no records found
    {
      val mySelect = transactor
        .use { xa =>
          fr"""select id from city where id in( $randKey1, $randKey2)""".stripMargin
            .query[Int]
            .to[List]
            .transact(xa)
        }
        .unsafeRunSync()
      assert(mySelect.size == 2)
    }

  }

}
