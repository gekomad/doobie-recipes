import java.io.{File, FileInputStream, FileOutputStream, PrintWriter}
import java.nio.file.StandardOpenOption

import com.github.gekomad.ittocsv.core.Header.csvHeader
import com.github.gekomad.ittocsv.core.ParseFailure
import org.scalatest.FunSuite

import scala.util.Try

class CSV extends FunSuite {

  import MyPredef.transactor

  case class Test2(field1: Int, field2: Option[String], field3: Option[Int])

  test("create csv file to disk, read it and insert in table using itto-csv") {
    object ReadCsvAndWriteDB {

      import cats.data.NonEmptyList
      import com.github.gekomad.ittocsv.parser.IttoCSVFormat
      import scala.collection.immutable
      import scala.concurrent.ExecutionContextExecutorService

      import com.github.gekomad.ittocsv.core.Header.{FieldNames, csvHeader}
      import java.nio.file.Paths
      import java.util.concurrent.Executors
      import com.github.gekomad.ittocsv.core.FromCsv
      import com.github.gekomad.ittocsv.core.Schema
      import cats.effect.IO
      import fs2.{io, text}

      import scala.concurrent.ExecutionContext

      val blockingExecutionContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

      import cats.effect.ContextShift

      implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

      import cats.implicits._
      import doobie.implicits._
      import doobie.util.update.Update

      val maxRowsToCommit = 100

      import com.github.gekomad.ittocsv.core.FromCsv._

      implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default

      //read from file N rows and store in db
      def readCsvAndWriteDB[B: doobie.util.Read: doobie.util.Write: FieldNames: Schema](inOutFile: String, sql: String): IO[Unit] = {
        import cats.instances.list._

        import cats.syntax.traverse._

        def bulkInsert[A: doobie.util.Read: doobie.util.Write: FieldNames: Schema](csvList: List[String], count: Long): Either[NonEmptyList[ParseFailure], IO[Int]] = {
          val csv =
            if (count == 0)
              csvList.drop(1)
            else csvList

          //write the list into db
          val x: Either[NonEmptyList[ParseFailure], List[A]] = csv.map(fromCsv[A](_).head).sequence

          x match {
            case Right(t) =>
              val dd = transactor.use { xa =>
                Update[A](sql).updateMany(t).transact(xa)
              }
              Right(dd)
            case Left(e) =>
              println("err: " + e)
              Left(e)
          }
        }

        io.file
          .readAll[IO](Paths.get(inOutFile), blockingExecutionContext, 4096)
          .through(text.utf8Decode)
          .through(text.lines)
          .chunkN(maxRowsToCommit)
          .zipWithIndex
          .map(chunk => bulkInsert[B](chunk._1.toList, chunk._2).map(_.unsafeRunSync))
          .compile
          .drain
      }
    }

    import java.nio.file.Paths

    import com.github.gekomad.ittocsv.parser.IttoCSVFormat
    import RandomUtil._
    import cats.effect.IO

    import cats.effect.ContextShift

    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

    import cats.implicits._
    import doobie.implicits._

    val nRecords  = 1000
    val inOutFile = s"${MyPredef.tmpDir}/test2.csv"

    import com.github.gekomad.ittocsv.core.Header.csvHeader
    import com.github.gekomad.ittocsv.core.ToCsv._
    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default

    def tableTest2: List[Test2] =
      transactor.use { xa =>
        sql"select field1, field2, field3 from test2 order by 1"
          .query[Test2]
          .to[List]
          .transact(xa)
      }.unsafeRunSync

    //create csv file
    val test2List = (0 until nRecords).toList.map { count =>
      Test2(count, getRandomOptionString(10 + getRandomInt(100)), getRandomOptionInt(0, 2000))
    }

    val content = test2List.foldLeft(csvHeader[Test2])((a, b) => a + IttoCSVFormat.default.recordSeparator + toCsv(b)).getBytes

    java.nio.file.Files.write(Paths.get(inOutFile), content)

    //create table
    assert(0 == {
      val drop = sql"""DROP TABLE IF EXISTS test2""".update.run

      val create =
        sql"""
        CREATE TABLE test2 (
        field1 SMALLINT,
        field2 VARCHAR,
        field3 SMALLINT
        )
      """.update.run

      transactor.use { xa =>
        (drop, create).mapN(_ + _).transact(xa)
      }.unsafeRunSync
    })

    assert(tableTest2 == Nil)

    ReadCsvAndWriteDB.readCsvAndWriteDB[Test2](inOutFile, "insert into test2 (field1, field2, field3) values (?, ?, ?)").unsafeRunSync

    assert(tableTest2 == test2List)
    assert(tableTest2.size == nRecords)
  }

  test("itto-csv test") {
    import com.github.gekomad.ittocsv.parser.IttoCSVFormat
    import com.github.gekomad.ittocsv.core.ToCsv._
    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default.withPrintHeader(false)
    case class Bar(a: String, b: Int, c: Long)

    val l: List[Bar] = List(Bar("Ye,llow", 3, 5L), Bar("""B,"oo""", 7, 6L), Bar("Hi", 7, 16L))

    val csv = toCsvL(l)

    assert(csv == "\"Ye,llow\",3,5\r\n\"B,\"\"oo\",7,6\r\nHi,7,16")
  }

  test("abstract select") {
    import scala.collection.immutable
    import doobie.implicits._
    import doobie.util.Read
    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])
    def mySelect[A: Read]: immutable.Seq[Any] =
      transactor
        .use { xa =>
          sql"select code, name, population, gnp from country"
            .query[A]
            .to[List]
            .transact(xa)
        }
        .unsafeRunSync
        .take(3)

    assert(
      mySelect[Country] == List(
        Country("AFG", "Afghanistan", 22720000, Some(5976.0)),
        Country("NLD", "Netherlands", 15864000, Some(371362.0)),
        Country("ANT", "Netherlands Antilles", 217000, Some(1941.0))
      )
    )
  }

  test("spool csv Parameterized") {
    import com.github.gekomad.ittocsv.parser.IttoCSVFormat
    import java.nio.file.Paths
    import java.util.concurrent.Executors
    import cats.effect.IO
    import doobie.implicits._
    import fs2.{io, text}
    import scala.concurrent.ExecutionContext
    import doobie.{HC, HPS}

    import cats.effect.ContextShift
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
    import com.github.gekomad.ittocsv.core.ToCsv._
    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default
    val blockingExecutionContext          = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
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
    val fileName = s"${MyPredef.tmpDir}/country2.out"

    {
      for {
        _ <- Util.writeIttoHeaderTofile[Country](fileName)
        _ <- transactor.use { xa =>
          HC.stream[Country](q, HPS.set((150000000, 200000000)), 512)
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
          |BRA,Brazil,170115000,776739.0
          |PAK,Pakistan,156483000,61289.0""".stripMargin
    )
  }

  test("spool empty csv Parameterized") {
    import com.github.gekomad.ittocsv.parser.IttoCSVFormat
    import java.nio.file.Paths
    import java.util.concurrent.Executors
    import cats.effect.IO
    import doobie.implicits._
    import fs2.{io, text}
    import scala.concurrent.ExecutionContext
    import doobie.{HC, HPS}

    import cats.effect.ContextShift
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
    import com.github.gekomad.ittocsv.core.ToCsv._
    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default
    val blockingExecutionContext          = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
    case class Country(code: String, name: String, pop: Int, gnp: Option[Double])
    val q =
      """
      select code, name, population, gnp
      from country
      where population < ?
      """
    val fileName = s"${MyPredef.tmpDir}/country2.out"

    val o = for {
      _ <- Util.writeIttoHeaderTofile[Country](fileName)
      _ <- transactor
        .use { xa =>
          HC.stream[Country](q, HPS.set(-1), 512)
            .transact(xa)
            .through(_.map(toCsv(_, true)))
            .through(text.utf8Encode)
            .through(io.file.writeAll[IO](Paths.get(fileName), blockingExecutionContext, Seq(StandardOpenOption.APPEND)))
            .compile
            .drain
        }
      //TODO workaround for https://github.com/functional-streams-for-scala/fs2/pull/1381
      size <- IO(new File(fileName).length())
      _    <- if (size == 0) Util.writeIttoHeaderTofile[Country](fileName) else IO.unit
    } yield ()

    o.unsafeRunSync()
    val lines = scala.io.Source.fromFile(fileName).getLines.mkString("\n")
    assert(lines == "code,name,pop,gnp")

  }
}
