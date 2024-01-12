package csv

import doobierecipes.Util._
import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.IO
import com.github.gekomad.ittocsv.core.ToCsv._
import com.github.gekomad.ittocsv.parser.IttoCSVFormat
import doobie.implicits._
import doobie.{HC, HPS}
import fs2.io.file.Files
import fs2.text

/**
  * create the file country1.csv reading country table
  */
class SpoolParameterized extends AnyFunSuite {

  test("spool csv Parameterized") {

    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])
    val q =
      """
      select code, name, population, gnp
      from country
      where population > ?
      and   population < ?
      order by code
      limit 3
      """
    val fileName = s"$tmpDir/country2.csv"
    deleteFile(fileName)

    {
      for {
        _ <- writeIttoHeaderTofile[Country](fileName)
        _ <- doobierecipes.Transactor.transactor
          .use { xa =>
            HC.stream[Country](q, HPS.set((150000000, 200000000)), 512)
              .transact(xa)
              .through(_.map(toCsv(_, printRecordSeparator = true)))
              .through(text.utf8.encode)
              .through(Files[IO].writeAll(fs2.io.file.Path(fileName), fs2.io.file.Flags(fs2.io.file.Flag.Append)))
              .compile
              .drain
          }
      } yield ()
    }.unsafeRunSync()

    val f     = scala.io.Source.fromFile(fileName)
    val lines = f.getLines().mkString("\n")
    f.close()
    assert(
      lines ==
        """code,name,pop,gnp
          |BRA,Brazil,170115000,776739.0
          |PAK,Pakistan,156483000,61289.0""".stripMargin
    )
  }

}
