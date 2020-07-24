package csv

import doobie.implicits._
import doobie.util.Read
import scala.collection.immutable
import org.scalatest.funsuite.AnyFunSuite

/**
  * Select data and populate the type A
  */
class GenericSelect extends AnyFunSuite {

  test("generic select") {

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    def mySelect[A: Read]: immutable.Seq[Any] = {
      doobierecipes.Transactor.transactor
        .use { xa =>
          sql"select code, name, population, gnp from country"
            .query[A]
            .to[List]
            .transact(xa)
        }
        .unsafeRunSync()
        .take(3)
    }

    assert(
      mySelect[Country] == List(
        Country("AFG", "Afghanistan", 22720000, Some(5976.0)),
        Country("NLD", "Netherlands", 15864000, Some(371362.0)),
        Country("ANT", "Netherlands Antilles", 217000, Some(1941.0))
      )
    )
  }

}
