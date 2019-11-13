package csv

import java.nio.file.StandardOpenOption
import cats.implicits._
import doobierecipes.Util._
import fs2.Stream
import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.Paths
import cats.effect.IO
import com.github.gekomad.ittocsv.core.ToCsv._
import com.github.gekomad.ittocsv.parser.IttoCSVFormat
import doobie.implicits._
import doobie.{HC, HPS}
import fs2.{io, text}
import cats.effect.ContextShift
import cats.effect.{Blocker, ExitCode}

/**
 * create the file country1.csv reading country table
 */

class SpoolParameterized extends AnyFunSuite {

  test("spool csv Parameterized") {

    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

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
    val fileName = s"${tmpDir}/country2.csv"
    deleteFile(fileName)
    val a1 = Stream.resource(Blocker[IO]).map { blocker =>
      {
        for {
          _ <- writeIttoHeaderTofile[Country](fileName)
          _ <- doobierecipes.Transactor.transactor
            .use { xa =>
              HC.stream[Country](q, HPS.set((150000000, 200000000)), 512)
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
    a1.compile.drain.as(ExitCode.Success).unsafeRunSync()
    val f     = scala.io.Source.fromFile(fileName)
    val lines = f.getLines.mkString("\n")
    f.close()
    assert(
      lines ==
        """code,name,pop,gnp
          |BRA,Brazil,170115000,776739.0
          |PAK,Pakistan,156483000,61289.0""".stripMargin
    )
  }

}
