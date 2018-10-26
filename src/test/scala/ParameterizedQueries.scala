import doobie.implicits._
import org.scalatest.FunSuite

class ParameterizedQueries extends FunSuite {

  import MyPredef.xa

  test("bigger than") {

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    def biggerThan(minPop: Int): List[Country] = sql"""
      select code, name, population, gnp
      from country
      where population > $minPop
      """.query[Country]
      .to[List] // ConnectionIO[List[Country]]
      .transact(xa) // IO[List[Country]]
      .unsafeRunSync // List[Country]]
      .take(2) // List[Country]]

    val mySelect = biggerThan(150000000)

    assert(mySelect == List(Country("BRA", "Brazil", 170115000, Some(776739.0)), Country("IDN", "Indonesia", 212107000, Some(84982.0))))

  }


  test("IN Clauses") {

    import cats.data.NonEmptyList

    import doobie._, doobie.implicits._

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    def populationIn(range: Range, codes: NonEmptyList[String]) = {
      val q =
        fr"""
        select code, name, population, gnp
        from country
        where population > ${range.min}
        and   population < ${range.max}
        and   """ ++ Fragments.in(fr"code", codes) // code IN (...)
      q.query[Country]
    }

    val mySelect: List[Country] = populationIn(100000000 to 300000000, NonEmptyList.of("USA", "BRA", "PAK", "GBR"))
      .to[List]
      .transact(xa) // IO[List[Country]]
      .unsafeRunSync // List[Country]]
      .take(2) // List[Country]]ConnectionIO

    assert(mySelect == List(Country("BRA", "Brazil", 170115000, Some(776739.0)), Country("PAK", "Pakistan", 156483000, Some(61289.0))))

  }

}


