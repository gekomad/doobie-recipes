import org.scalatest.FunSuite

class CSVgenerator extends FunSuite {

  import MyPredef.transactor

  test("spool csv") {

    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

    import java.nio.file.Paths
    import java.util.concurrent.Executors

    import _root_.io.chrisdavenport.cormorant._
    import _root_.io.chrisdavenport.cormorant.generic.semiauto._
    import _root_.io.chrisdavenport.cormorant.implicits._
    import cats.effect.IO
    import doobie.implicits._
    import fs2.{io, text}

    import scala.concurrent.ExecutionContext

    implicit val lr: LabelledRead[Country] = deriveLabelledRead
    implicit val lw: LabelledWrite[Country] = deriveLabelledWrite

    val blockingExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
    import cats.effect.ContextShift
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

    val printer = Printer.generic(";", "\n", "\"", "\"", Set("\r"))

    transactor.use { xa =>
      sql"select code, name, population, gnp from country"
        .query[Country]
        .stream
        .transact(xa)
        .through(_.map(_.write.print(printer)))
        .intersperse("\n")
        .through(text.utf8Encode)
        .through(io.file.writeAll[IO](Paths.get("/tmp/output.csv"), blockingExecutionContext))
        .compile.drain
    }.unsafeRunSync()

  }


}


