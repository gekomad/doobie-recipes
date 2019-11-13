package parameterizedQueries
import doobierecipes.Transactor._
import doobie.implicits._
import org.scalatest.funsuite.AnyFunSuite

class Parameters extends AnyFunSuite {

  case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

  test("parameters") {
    import doobie.free.connection.ConnectionIO
    import fs2.Stream
    import doobie._

    val q =
      """
      select code, name, population, gnp
      from country
      where population > ?
      and   population < ?
      """

    def populationIn(range: Range): Stream[ConnectionIO, Country] =
      HC.stream[Country](q, HPS.set((range.min, range.max)), 512)

    val x = transactor.use { xa =>
      populationIn(150000000 to 200000000).compile.toList.transact(xa)
    }

    assert(
      x.unsafeRunSync() == List(
        Country("BRA", "Brazil", 170115000, Some(776739.0)),
        Country("PAK", "Pakistan", 156483000, Some(61289.0))
      )
    )
  }
}
