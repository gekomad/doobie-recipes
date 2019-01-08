import cats.effect._
import com.github.gekomad.ittocsv.parser.IttoCSVFormat
import doobie.implicits._
import doobie.util.Read
import org.scalatest.FunSuite

import scala.collection.immutable

class SelectingData extends FunSuite {

  import MyPredef.transactor

  case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

  test("itto-csv") {

    import com.github.gekomad.ittocsv.core.ToCsv._
    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default

    def mySelect[A: Read]: List[A] =
      transactor
        .use { xa =>
          sql"select code, name, population, gnp from country"
            .query[A]
            .to[List]
            .transact(xa)
        }
        .unsafeRunSync
        .take(3)

    val o: List[Country] = mySelect[Country]

    {
      //generic

      assert(toCsvL(o) == "code,name,pop,gnp\r\nAFG,Afghanistan,22720000,5976.0\r\nNLD,Netherlands,15864000,371362.0\r\nANT,Netherlands Antilles,217000,1941.0")
      assert(toCsv(o) == "AFG,Afghanistan,22720000,5976.0,NLD,Netherlands,15864000,371362.0,ANT,Netherlands Antilles,217000,1941.0")
    }

    {
      //tab
      import com.github.gekomad.ittocsv.core.ToCsv._
      implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.tab
      val p                                 = toCsvL(o)
      assert(p == "code\tname\tpop\tgnp\r\nAFG\tAfghanistan\t22720000\t5976.0\r\nNLD\tNetherlands\t15864000\t371362.0\r\nANT\tNetherlands Antilles\t217000\t1941.0")
    }
  }

  test("abstract select") {

    def mySelect[A: Read]: immutable.Seq[A] =
      transactor
        .use { xa =>
          sql"select code, name, population, gnp from country"
            .query[A]
            .to[List]
            .transact(xa)
        }
        .unsafeRunSync
        .take(3)

    assert(
      mySelect[Country] == List(
        Country("AFG", "Afghanistan", 22720000, Some(5976.0)),
        Country("NLD", "Netherlands", 15864000, Some(371362.0)),
        Country("ANT", "Netherlands Antilles", 217000, Some(1941.0))
      )
    )
  }

  test("select one column") {
    val mySelect: IO[List[String]] = transactor.use { xa =>
      sql"select name from country"
        .query[String] // Query0[String]
        .to[List] // ConnectionIO[List[String]]
        .transact(xa) // IO[List[String]]
    }

    assert(mySelect.unsafeRunSync.take(3) == List("Afghanistan", "Netherlands", "Netherlands Antilles"))
  }

  test("select multiple columns") {

    val mySelect: immutable.Seq[(String, String, Int, Option[Double])] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country"
          .query[(String, String, Int, Option[Double])]
          .to[List] // ConnectionIO[List[(String, String, Int, Option[Double])]]
          .transact(xa) // IO[List[(String, String, Int, Option[Double])]]
      }
      .unsafeRunSync // List[(String, String, Int, Option[Double])]
      .take(3) // List[(String, String, Int, Option[Double])]

    assert(mySelect == List(("AFG", "Afghanistan", 22720000, Some(5976.0)), ("NLD", "Netherlands", 15864000, Some(371362.0)), ("ANT", "Netherlands Antilles", 217000, Some(1941.0))))
  }

  test("row mappings") {
    import shapeless._

    val mySelect: immutable.Seq[String :: String :: Int :: Option[Double] :: HNil] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country"
          .query[String :: String :: Int :: Option[Double] :: HNil]
          .to[List] // ConnectionIO[List[String :: String :: Int :: Option[Double] :: HNil]]
          .transact(xa) // IO[List[String :: String :: Int :: Option[Double] :: HNil]]
      }
      .unsafeRunSync // List[String :: String :: Int :: Option[Double] :: HNil]]
      .take(3) // List[String :: String :: Int :: Option[Double] :: HNil]]

    assert(
      mySelect == List(
        "AFG" :: "Afghanistan" :: 22720000 :: Some(5976.0) :: HNil,
        "NLD" :: "Netherlands" :: 15864000 :: Some(371362.0) :: HNil,
        "ANT" :: "Netherlands Antilles" :: 217000 :: Some(1941.0) :: HNil
      )
    )
  }

  test("shapeless record") {
    import shapeless._
    import shapeless.record.Record

    type Rec = Record.`'code -> String, 'name -> String, 'pop -> Int, 'gnp -> Option[Double]`.T

    val mySelect: Seq[Rec] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country"
          .query[Rec]
          .to[List] // ConnectionIO[List[String :: String :: Int :: Option[Double] :: HNil]]
          .transact(xa) // IO[List[String :: String :: Int :: Option[Double] :: HNil]]
      }
      .unsafeRunSync // List[String :: String :: Int :: Option[Double] :: HNil]]
      .take(3) // List[String :: String :: Int :: Option[Double] :: HNil]]

    assert(
      mySelect == List(
        "AFG" :: "Afghanistan" :: 22720000 :: Some(5976.0) :: HNil,
        "NLD" :: "Netherlands" :: 15864000 :: Some(371362.0) :: HNil,
        "ANT" :: "Netherlands Antilles" :: 217000 :: Some(1941.0) :: HNil
      )
    )
  }

  test("mapping rows to a case class") {

    val mySelect: immutable.Seq[Country] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country"
          .query[Country]
          .to[List] // ConnectionIO[List[Country]]
          .transact(xa) // IO[List[Country]]
      }
      .unsafeRunSync // List[Country]]
      .take(3) // List[Country]]

    assert(
      mySelect == List(Country("AFG", "Afghanistan", 22720000, Some(5976.0)), Country("NLD", "Netherlands", 15864000, Some(371362.0)), Country("ANT", "Netherlands Antilles", 217000, Some(1941.0)))
    )
  }

  test("nested case class") {

    case class Code(code: String)
    case class Country(name: String, pop: Int, gnp: Option[Double])

    val mySelect: Seq[(Code, Country)] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country"
          .query[(Code, Country)]
          .to[List] // ConnectionIO[List[(Code, Country)]]
          .transact(xa) // IO[List[(Code, Country)]]
      }
      .unsafeRunSync // List[(Code, Country)]]
      .take(3) // List[(Code, Country)]]

    assert(
      mySelect == List(
        (Code("AFG"), Country("Afghanistan", 22720000, Some(5976.0))),
        (Code("NLD"), Country("Netherlands", 15864000, Some(371362.0))),
        (Code("ANT"), Country("Netherlands Antilles", 217000, Some(1941.0)))
      )
    )
  }

  test("nested case class Map") {

    case class Code(code: String)
    case class Country(name: String, pop: Int, gnp: Option[Double])

    val mySelect: Map[Code, Country] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country"
          .query[(Code, Country)]
          .to[List] // ConnectionIO[List[(Code, Country)]]
          .transact(xa) // IO[List[(Code, Country)]]
      }
      .map(_.toMap)  //IO[Map[Code, Country]]
      .unsafeRunSync // IO[Map[Code, Country]]
      .take(3) // IO[Map[Code, Country]]

    assert(
      mySelect == Map(
        Code("IRL") -> Country("Ireland", 3775100, Some(75921.0)),
        Code("SLV") -> Country("El Salvador", 6276000, Some(11863.0)),
        Code("GTM") -> Country("Guatemala", 11385000, Some(19008.0))
      )
    )
  }

  test("streaming") {

    val mySelect = transactor.use { xa =>
      sql"select code, name, population, gnp from country"
        .query[Country]
        .to[List]
        .transact(xa)
    }

    val o: immutable.Seq[Country] = mySelect.unsafeRunSync.take(3)

    assert(o == List(Country("AFG", "Afghanistan", 22720000, Some(5976.0)), Country("NLD", "Netherlands", 15864000, Some(371362.0)), Country("ANT", "Netherlands Antilles", 217000, Some(1941.0))))
  }

  test("join") {
    import MyPredef.transactor
    import doobie.implicits._

    case class Country(name: String, code: String)
    case class City(name: String, district: String)

    val join = transactor.use { xa =>
      sql"""
      select c.name, c.code,
           k.name, k.district
      from country c
      left outer join city k
      on c.capital = k.id
      order by c.code desc
      """
        .query[(Country, Option[City])]
        .to[List]
        .transact(xa)
    }

    assert(join.unsafeRunSync.take(2) == List((Country("Zimbabwe", "ZWE"), Some(City("Harare", "Harare"))), (Country("Zambia", "ZMB"), Some(City("Lusaka", "Lusaka")))))

  }

}
