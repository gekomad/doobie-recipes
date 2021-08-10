import doobierecipes.Transactor._
import cats.effect.IO
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global

class Logging extends AnyFunSuite {

  case class Person(id: Int, name: String)

  test("logging") {

    import doobie.implicits._
    import doobie.util.log.LogHandler

    def byName(pat: String): IO[List[(String, String)]] = {
      transactor.use { xa =>
        sql"select name, code from country where name like $pat"
          .queryWithLogHandler[(String, String)](LogHandler.jdkLogHandler)
          .to[List]
          .transact(xa)
      }
    }

    assert(byName("U%").unsafeRunSync().take(2) == List(("United Arab Emirates", "ARE"), ("United Kingdom", "GBR")))
  }

  test("implicit logging") {

    import doobie.implicits._
    import doobie.util.log.LogHandler

    implicit val han: LogHandler = LogHandler.jdkLogHandler

    def byName(pat: String) = {
      transactor.use { xa =>
        sql"select name, code from country where name like $pat"
          .query[(String, String)] // handler will be picked up here
          .to[List]
          .transact(xa)
      }
    }

    assert(byName("U%").unsafeRunSync().take(2) == List(("United Arab Emirates", "ARE"), ("United Kingdom", "GBR")))
  }

}
