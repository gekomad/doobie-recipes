package parameterizedQueries
import doobierecipes.Transactor._
import doobie.implicits._
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global

class BiggerThan extends AnyFunSuite {

  case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

  test("bigger than") {

    def biggerThan(minPop: Int): List[Country] =
      transactor
        .use { xa =>
          sql"""select code, name, population, gnp
               |  from country
               |  where population > $minPop""".stripMargin
            .query[Country]
            .to[List]
            .transact(xa)
        }
        .unsafeRunSync()
        .take(2)

    val mySelect = biggerThan(150000000)

    assert(
      mySelect == List(
        Country("BRA", "Brazil", 170115000, Some(776739.0)),
        Country("IDN", "Indonesia", 212107000, Some(84982.0))
      )
    )
  }
}
