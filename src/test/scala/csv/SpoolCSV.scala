package csv

import java.nio.file.StandardOpenOption

import cats.effect.Blocker
import cats.implicits._
import com.github.gekomad.ittocsv.core.CsvStringEncoder
import com.github.gekomad.ittocsv.core.Header.FieldNames
import doobierecipes.Transactor._
import doobierecipes.Util
import fs2.Stream
import org.scalatest.funsuite.AnyFunSuite

/**
  * create the file country1.csv reading country table
  */
class SpoolCSV extends AnyFunSuite {

  import java.nio.file.Paths

  import cats.effect.IO
  import com.github.gekomad.ittocsv.core.ToCsv._
  import com.github.gekomad.ittocsv.parser.IttoCSVFormat
  import doobie.implicits._
  import doobie.util.fragment.Fragment
  import fs2.{io, text}
  implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default

  def spool[A: doobie.util.Read: FieldNames: CsvStringEncoder](query: String, fileName: String): Stream[IO, IO[Unit]] =
    Stream.resource(Blocker[IO]).map { blocker =>
      for {
        _ <- Util.writeIttoHeaderTofile[A](fileName)
        _ <- transactor
          .use { xa =>
            Fragment
              .const(query)
              .query[A]
              .stream
              .transact(xa)
              .through(_.map(toCsv(_, printRecordSeparator = true)))
              .through(text.utf8Encode)
              .through(io.file.writeAll[IO](Paths.get(fileName), blocker, Seq(StandardOpenOption.APPEND)))
              .compile
              .drain
          }
      } yield ()
    }

  test("spool csv") {

    val fileName = s"${Util.tmpDir}/country1.csv"
    Util.deleteFile(fileName)

    val q = "select code, name, population, gnp from country order by code limit 3"
    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    spool[Country](q, fileName).map(_.unsafeRunSync).compile.drain.unsafeRunSync

    val f     = scala.io.Source.fromFile(fileName)
    val lines = f.getLines.mkString("\n")
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
