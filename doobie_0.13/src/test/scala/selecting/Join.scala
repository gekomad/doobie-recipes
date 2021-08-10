import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite

class Join extends AnyFunSuite {

  test("join") {

    import doobie.implicits._

    case class Country(name: String, code: String)
    case class City(name: String, district: String)

    val join = transactor.use { xa =>
      sql"""
           |      select c.name, c.code, k.name, k.district
           |      from country c
           |      left outer join city k
           |      on c.capital = k.id
           |      order by c.code desc""".stripMargin
        .query[(Country, Option[City])]
        .to[List]
        .transact(xa)
    }

    assert(
      join.unsafeRunSync().take(2) == List(
        (Country("Zimbabwe", "ZWE"), Some(City("Harare", "Harare"))),
        (Country("Zambia", "ZMB"), Some(City("Lusaka", "Lusaka")))
      )
    )

  }

}
