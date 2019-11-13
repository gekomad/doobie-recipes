package selecting

import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.immutable

class StatementFragments extends AnyFunSuite {

  test("select with fragments") {
    import cats.implicits._
    import doobie._
    import Fragments.{in, whereAndOpt}
    import doobie.implicits._

    // Country Info
    case class Info(name: String, code: String, population: Int)

    // Construct a Query0 with some optional filter conditions and a configurable LIMIT.
    def select2Rows(name: Option[String], pop: Option[Int], codes: List[String], limit: Long): immutable.Seq[Info] = {

      // Three Option[Fragment] filter conditions.
      val f1 = name.map(s => fr"name LIKE $s")
      val f2 = pop.map(n => fr"population > $n")
      val f3 = codes.toNel.map(cs => in(fr"code", cs))

      // Our final query
      val q: Fragment =
        fr"SELECT name, code, population FROM country" ++
          whereAndOpt(f1, f2, f3) ++
          fr"LIMIT $limit"

      transactor
        .use { xa =>
          q.query[Info]
            .to[List] // ConnectionIO[List[String]]
            .transact(xa) // IO[List[String]]
        }
        .unsafeRunSync // List[String]
        .take(2) // List[Strings]

    }

    assert(
      select2Rows(None, None, Nil, 10) == List(
        Info("Afghanistan", "AFG", 22720000),
        Info("Netherlands", "NLD", 15864000)
      )
    )
    assert(
      select2Rows(Some("U%"), None, Nil, 10) == List(
        Info("United Arab Emirates", "ARE", 2441000),
        Info("United Kingdom", "GBR", 59623400)
      )
    )
    assert(
      select2Rows(Some("U%"), Some(12345), List("FRA", "GBR"), 10) == List(Info("United Kingdom", "GBR", 59623400))
    )
  }

}
