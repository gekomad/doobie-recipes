package parameterizedQueries
import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global

class INClauses extends AnyFunSuite {

  case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

  test("IN Clauses") {

    import cats.data.NonEmptyList

    import doobie._, doobie.implicits._

    def populationIn(range: Range, codes: NonEmptyList[String]) = {
      val q =
        fr"""
        select code, name, population, gnp
        from country
        where population > ${range.min}
        and   population < ${range.max}
        and   """ ++ Fragments.in(fr"code", codes)
      q.query[Country]
    }

    val mySelect: List[Country] = transactor
      .use { xa =>
        populationIn(100000000 to 300000000, NonEmptyList.of("USA", "BRA", "PAK", "GBR"))
          .to[List]
          .transact(xa)
      }
      .unsafeRunSync()
      .take(2)

    assert(
      mySelect == List(
        Country("BRA", "Brazil", 170115000, Some(776739.0)),
        Country("PAK", "Pakistan", 156483000, Some(61289.0))
      )
    )

  }

}
