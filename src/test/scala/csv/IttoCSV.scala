package csv

import com.github.gekomad.ittocsv.parser.IttoCSVFormat
import doobie.implicits._
import doobie.util.Read
import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global

/**
  * Select data populate List[A] and convert it to CSV using IttoCSV
  */
class IttoCSV extends AnyFunSuite {

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
        .unsafeRunSync()
        .take(3)

    val o: List[Country] = mySelect[Country]

    {
      //generic

      assert(
        toCsvL(o) == "code,name,pop,gnp\r\nAFG,Afghanistan,22720000,5976.0\r\nNLD,Netherlands,15864000,371362.0\r\nANT,Netherlands Antilles,217000,1941.0"
      )
      assert(
        toCsv(o) == "AFG,Afghanistan,22720000,5976.0,NLD,Netherlands,15864000,371362.0,ANT,Netherlands Antilles,217000,1941.0"
      )
    }

    {
      //using tab separator
      import com.github.gekomad.ittocsv.core.ToCsv._
      implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.tab
      val p                                 = toCsvL(o)
      assert(
        p == "code\tname\tpop\tgnp\r\nAFG\tAfghanistan\t22720000\t5976.0\r\nNLD\tNetherlands\t15864000\t371362.0\r\nANT\tNetherlands Antilles\t217000\t1941.0"
      )
    }
  }

}
