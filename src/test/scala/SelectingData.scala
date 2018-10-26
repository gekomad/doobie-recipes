import cats.effect._
import doobie.implicits._
import org.scalatest.FunSuite

import scala.collection.immutable

class SelectingData extends FunSuite {

  import MyPredef.xa

  test("select 1 column") {

    val mySelect: Seq[String] = sql"select name from country"
      .query[String] // Query0[String]
      .to[List] // ConnectionIO[List[String]]
      .transact(xa) // IO[List[String]]
      .unsafeRunSync // List[String]
      .take(3) // List[String]

    assert(mySelect == List("Afghanistan", "Netherlands", "Netherlands Antilles"))
  }

  test("select multiple columns") {

    val mySelect: immutable.Seq[(String, String, Int, Option[Double])] = sql"select code, name, population, gnp from country"
      .query[(String, String, Int, Option[Double])]
      .to[List] // ConnectionIO[List[(String, String, Int, Option[Double])]]
      .transact(xa) // IO[List[(String, String, Int, Option[Double])]]
      .unsafeRunSync // List[(String, String, Int, Option[Double])]
      .take(3) // List[(String, String, Int, Option[Double])]

    assert(mySelect == List(("AFG", "Afghanistan", 22720000, Some(5976.0)), ("NLD", "Netherlands", 15864000, Some(371362.0)), ("ANT", "Netherlands Antilles", 217000, Some(1941.0))))
  }

  test("row mappings") {
    import shapeless._

    val mySelect: immutable.Seq[String :: String :: Int :: Option[Double] :: HNil] = sql"select code, name, population, gnp from country"
      .query[String :: String :: Int :: Option[Double] :: HNil]
      .to[List] // ConnectionIO[List[String :: String :: Int :: Option[Double] :: HNil]]
      .transact(xa) // IO[List[String :: String :: Int :: Option[Double] :: HNil]]
      .unsafeRunSync // List[String :: String :: Int :: Option[Double] :: HNil]]
      .take(3) // List[String :: String :: Int :: Option[Double] :: HNil]]

    assert(mySelect == List("AFG" :: "Afghanistan" :: 22720000 :: Some(5976.0) :: HNil, "NLD" :: "Netherlands" :: 15864000 :: Some(371362.0) :: HNil, "ANT" :: "Netherlands Antilles" :: 217000 :: Some(1941.0) :: HNil)
    )
  }

  test("shapeless record") {
    import shapeless._
    import shapeless.record.Record

    type Rec = Record.`'code -> String, 'name -> String, 'pop -> Int, 'gnp -> Option[Double]`.T

    val mySelect: Seq[Rec] = sql"select code, name, population, gnp from country"
      .query[Rec]
      .to[List] // ConnectionIO[List[String :: String :: Int :: Option[Double] :: HNil]]
      .transact(xa) // IO[List[String :: String :: Int :: Option[Double] :: HNil]]
      .unsafeRunSync // List[String :: String :: Int :: Option[Double] :: HNil]]
      .take(3) // List[String :: String :: Int :: Option[Double] :: HNil]]

    assert(mySelect == List("AFG" :: "Afghanistan" :: 22720000 :: Some(5976.0) :: HNil, "NLD" :: "Netherlands" :: 15864000 :: Some(371362.0) :: HNil, "ANT" :: "Netherlands Antilles" :: 217000 :: Some(1941.0) :: HNil)
    )
  }

  test("mapping rows to a case class") {

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    val mySelect: immutable.Seq[Country] = sql"select code, name, population, gnp from country"
      .query[Country]
      .to[List] // ConnectionIO[List[Country]]
      .transact(xa) // IO[List[Country]]
      .unsafeRunSync // List[Country]]
      .take(3) // List[Country]]

    assert(mySelect == List(Country("AFG", "Afghanistan", 22720000, Some(5976.0)), Country("NLD", "Netherlands", 15864000, Some(371362.0)), Country("ANT", "Netherlands Antilles", 217000, Some(1941.0))))
  }

  test("nested case class") {

    case class Code(code: String)
    case class Country(name: String, pop: Int, gnp: Option[Double])

    val mySelect: Seq[(Code, Country)] = sql"select code, name, population, gnp from country"
      .query[(Code, Country)]
      .to[List] // ConnectionIO[List[(Code, Country)]]
      .transact(xa) // IO[List[(Code, Country)]]
      .unsafeRunSync // List[(Code, Country)]]
      .take(3) // List[(Code, Country)]]

    assert(mySelect == List((Code("AFG"), Country("Afghanistan", 22720000, Some(5976.0))), (Code("NLD"), Country("Netherlands", 15864000, Some(371362.0))), (Code("ANT"), Country("Netherlands Antilles", 217000, Some(1941.0)))))
  }

  test("nested case class Map") {

    case class Code(code: String)
    case class Country(name: String, pop: Int, gnp: Option[Double])

    val mySelect: Map[Code, Country] = sql"select code, name, population, gnp from country"
      .query[(Code, Country)]
      .to[List] // ConnectionIO[List[(Code, Country)]]
      .transact(xa) // IO[List[(Code, Country)]]
      .map(_.toMap) //IO[Map[Code, Country]]
      .unsafeRunSync // IO[Map[Code, Country]]
      .take(3) // IO[Map[Code, Country]]

    assert(mySelect == Map(Code("IRL") -> Country("Ireland", 3775100, Some(75921.0)), Code("SLV") -> Country("El Salvador", 6276000, Some(11863.0)), Code("GTM") -> Country("Guatemala", 11385000, Some(19008.0))))
  }

  test("streaming") {

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    val mySelect: fs2.Stream[IO, Country] = sql"select code, name, population, gnp from country"
      .query[Country]
      .stream
      .transact(xa)

    val o: immutable.Seq[Country] = mySelect.take(3).compile.toList.unsafeRunSync

    assert(o == List(Country("AFG", "Afghanistan", 22720000, Some(5976.0)), Country("NLD", "Netherlands", 15864000, Some(371362.0)), Country("ANT", "Netherlands Antilles", 217000, Some(1941.0))))
  }

}


