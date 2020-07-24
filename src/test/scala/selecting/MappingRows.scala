import doobie.implicits._
import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.immutable

class MappingRows extends AnyFunSuite {

  test("mapping rows to a case class") {
    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])
    val mySelect: immutable.Seq[Country] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country"
          .query[Country]
          .to[List]
          .transact(xa)
      }
      .unsafeRunSync()
      .take(3)

    assert(
      mySelect == List(
        Country("AFG", "Afghanistan", 22720000, Some(5976.0)),
        Country("NLD", "Netherlands", 15864000, Some(371362.0)),
        Country("ANT", "Netherlands Antilles", 217000, Some(1941.0))
      )
    )
  }

}
