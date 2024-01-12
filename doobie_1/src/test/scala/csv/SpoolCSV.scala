package csv

import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global
import doobierecipes.Transactor._
import doobierecipes.Util
import fs2.io.file.Files

/**
  * create the file country1.csv reading country table
  */
class SpoolCSV extends AnyFunSuite {

  case class Test2(field1: Int, field2: Option[String], field3: Option[Int])

  test("spool csv") {

    import cats.effect.IO
    import com.github.gekomad.ittocsv.core.ToCsv._
    import com.github.gekomad.ittocsv.parser.IttoCSVFormat
    import doobie.implicits._
    import doobie.util.fragment.Fragment
    import fs2.text
    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    val fileName = s"${Util.tmpDir}/country1.csv"
    Util.deleteFile(fileName)
    val q = "select code, name, population, gnp from country order by code limit 3"
    val x =
      for {
        _ <- Util.writeIttoHeaderTofile[Country](fileName)
        _ <- transactor
          .use { xa =>
            Fragment
              .const(q)
              .query[Country]
              .stream
              .transact(xa)
              .through(_.map(toCsv(_, printRecordSeparator = true)))
              .through(text.utf8.encode)
              .through(Files[IO].writeAll(fs2.io.file.Path(fileName), fs2.io.file.Flags(fs2.io.file.Flag.Append)))
              .compile
              .drain
          }
      } yield ()
    x.unsafeRunSync()

    val f     = scala.io.Source.fromFile(fileName)
    val lines = f.getLines().mkString("\n")
    f.close()

    assert(
      lines ==
        """code,name,pop,gnp
          |ABW,Aruba,103000,828.0
          |AFG,Afghanistan,22720000,5976.0
          |AGO,Angola,12878000,6648.0""".stripMargin
    )
  }
}
