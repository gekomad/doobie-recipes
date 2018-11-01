import doobie.util.Read
import io.chrisdavenport.cormorant.{LabelledRead, LabelledWrite}
import io.chrisdavenport.cormorant.generic.semiauto.{deriveLabelledRead, deriveLabelledWrite}
import org.scalatest.FunSuite

import scala.collection.immutable

class CSVgenerator extends FunSuite {

  import MyPredef.transactor

  case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

  test("abstract select") {

    import doobie.implicits._
    import doobie.util.Read

    def mySelect[A: Read]: immutable.Seq[Any] = transactor.use { xa =>
      sql"select code, name, population, gnp from country"
        .query[A]
        .to[List]
        .transact(xa)
    }
      .unsafeRunSync
      .take(3)

    assert(mySelect[Country] == List(Country("AFG", "Afghanistan", 22720000, Some(5976.0)), Country("NLD", "Netherlands", 15864000, Some(371362.0)), Country("ANT", "Netherlands Antilles", 217000, Some(1941.0))))
  }

  test("spool csv abstract") {

    import _root_.io.chrisdavenport.cormorant._
    import _root_.io.chrisdavenport.cormorant.generic.semiauto._
    import _root_.io.chrisdavenport.cormorant.implicits._

    def spool[A: doobie.util.Read : LabelledWrite]: Unit = {

      import java.nio.file.Paths
      import java.util.concurrent.Executors
      import cats.effect.IO
      import doobie.implicits._
      import fs2.{io, text}
      import scala.concurrent.ExecutionContext

      val blockingExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
      import cats.effect.ContextShift
      implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

      val printer = Printer.generic(";", "\n", "\"", "\"", Set("\r"))

      transactor.use { xa =>
        sql"select code, name, population, gnp from country"
          .query[A]
          .stream
          .transact(xa)
          .through(_.map(_.write.print(printer)))
          .intersperse("\n")
          .through(text.utf8Encode)
          .through(io.file.writeAll[IO](Paths.get("/tmp/output.csv"), blockingExecutionContext))
          .compile.drain
      }.unsafeRunSync()
    }

    implicit val lr: LabelledRead[Country] = deriveLabelledRead
    implicit val lw: LabelledWrite[Country] = deriveLabelledWrite

    spool[Country]
  }

  test("spool csv Parameterized") {

    import _root_.io.chrisdavenport.cormorant._
    import _root_.io.chrisdavenport.cormorant.generic.semiauto._
    import _root_.io.chrisdavenport.cormorant.implicits._
    import java.nio.file.Paths
    import java.util.concurrent.Executors
    import cats.effect.IO
    import doobie.implicits._
    import fs2.{io, text}
    import scala.concurrent.ExecutionContext
    import doobie.{HC, HPS}

    import cats.effect.ContextShift
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
    val printer = Printer.generic(";", "\n", "\"", "\"", Set("\r"))

    val blockingExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

    implicit val lr: LabelledRead[Country] = deriveLabelledRead
    implicit val lw: LabelledWrite[Country] = deriveLabelledWrite

    val q =
      """
      select code, name, population, gnp
      from country
      where population > ?
      and   population < ?
      """

    transactor.use { xa =>
      HC.stream[Country](q, HPS.set((150000000, 200000000)), 512)
        .transact(xa)
        .through(_.map(_.write.print(printer)))
        .intersperse("\n")
        .through(text.utf8Encode)
        .through(io.file.writeAll[IO](Paths.get("/tmp/country.out"), blockingExecutionContext))
        .compile.drain
    }.unsafeRunSync()


  }


}


