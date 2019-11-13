import doobie.implicits._
import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite

class NestedClass extends AnyFunSuite {

  test("nested case class") {

    case class Code(code: String)
    case class Country(name: String, pop: Int, gnp: Option[Double])

    val mySelect: Seq[(Code, Country)] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country"
          .query[(Code, Country)]
          .to[List]
          .transact(xa)
      }
      .unsafeRunSync
      .take(3)

    assert(
      mySelect == List(
        (Code("AFG"), Country("Afghanistan", 22720000, Some(5976.0))),
        (Code("NLD"), Country("Netherlands", 15864000, Some(371362.0))),
        (Code("ANT"), Country("Netherlands Antilles", 217000, Some(1941.0)))
      )
    )
  }

}
