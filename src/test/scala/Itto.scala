import java.nio.file.StandardOpenOption

import org.scalatest.FunSuite

class Itto extends FunSuite {

  import MyPredef.transactor

  case class Test2(field1: Int, field2: Option[String], field3: Option[Int])

  test("spool csv") {
    import java.nio.file.Paths
    import java.util.concurrent.Executors

    import cats.effect.{ContextShift, IO}
    import com.github.gekomad.ittocsv.parser.IttoCSVFormat
    import doobie.implicits._
    import doobie.util.fragment.Fragment
    import fs2.{io, text}

    import scala.concurrent.ExecutionContext
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
    import com.github.gekomad.ittocsv.core.ToCsv._
    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default
    val blockingExecutionContext          = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    val fileName = s"${MyPredef.tmpDir}/country1.out"

    val q = "select code, name, population, gnp from country order by code limit 3"

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
              .through(_.map(toCsv(_, true)))
              .through(text.utf8Encode)
              .through(io.file.writeAll[IO](Paths.get(fileName), blockingExecutionContext, Seq(StandardOpenOption.APPEND)))
              .compile
              .drain
          }
      } yield ()
    }.unsafeRunSync()

    val lines = scala.io.Source.fromFile(fileName).getLines.mkString("\n")
    assert(
      lines ==
        """code,name,pop,gnp
        |ABW,Aruba,103000,828.0
        |AFG,Afghanistan,22720000,5976.0
        |AGO,Angola,12878000,6648.0""".stripMargin
    )
  }

}
