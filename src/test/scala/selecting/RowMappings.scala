import doobie.implicits._
import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.immutable

class RowMappings extends AnyFunSuite {

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


}
