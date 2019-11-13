package csv

import java.nio.file.StandardOpenOption

import cats.effect.{Blocker, ExitCode}
import cats.implicits._
import doobierecipes.Transactor._
import doobierecipes.Util
import fs2.Stream
import org.scalatest.funsuite.AnyFunSuite

/**
* create the file country1.csv reading country table
 */
class SpoolCSV extends AnyFunSuite {

  case class Test2(field1: Int, field2: Option[String], field3: Option[Int])

  test("spool csv") {
    import java.nio.file.Paths

    import cats.effect.IO
    import com.github.gekomad.ittocsv.core.ToCsv._
    import com.github.gekomad.ittocsv.parser.IttoCSVFormat
    import doobie.implicits._
    import doobie.util.fragment.Fragment
    import fs2.{io, text}
    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    val fileName = s"${Util.tmpDir}/country1.csv"
    Util.deleteFile(fileName)
    val q = "select code, name, population, gnp from country order by code limit 3"
    val x = Stream.resource(Blocker[IO]).map { blocker =>
      {
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
                .through(text.utf8Encode)
                .through(io.file.writeAll[IO](Paths.get(fileName), blocker, Seq(StandardOpenOption.APPEND)))
                .compile
                .drain

            }
        } yield ()
      }.unsafeRunSync()
    }

    x.compile.drain.as(ExitCode.Success).unsafeRunSync()
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
