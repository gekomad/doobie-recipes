import doobie.implicits._
import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global

class NestedClassMap extends AnyFunSuite {

  test("nested case class Map") {

    case class Code(code: String)
    case class Country(name: String, pop: Int, gnp: Option[Double])

    val mySelect: Map[Code, Country] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country where code='ABW'"
          .query[(Code, Country)]
          .to[List] // ConnectionIO[List[(Code, Country)]]
          .transact(xa) // IO[List[(Code, Country)]]
      }
      .map(_.toMap) //IO[Map[Code, Country]]
      .unsafeRunSync() // IO[Map[Code, Country]]

    assert(mySelect == Map(Code("ABW") -> Country("Aruba", 103000, Some(828.00))))
  }

}
