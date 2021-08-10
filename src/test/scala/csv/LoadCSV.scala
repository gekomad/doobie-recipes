package csv

import com.github.gekomad.ittocsv.core.ParseFailure
import doobierecipes.Util._
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global
import fs2.io.file.Files

/**
  *
  * 1. create csv on disk
  * 2. read csv file and insert into table
  *
  */
class LoadCSV extends AnyFunSuite {

  case class Test2(field1: Int, field2: Option[String], field3: Option[Int])

  test("create csv file on disk, read it and insert in table using itto-csv") {
    object ReadCsvAndWriteDB {

      import cats.data.NonEmptyList
      import cats.effect.IO
      import com.github.gekomad.ittocsv.core.Header.FieldNames
      import com.github.gekomad.ittocsv.core.Schema
      import com.github.gekomad.ittocsv.parser.IttoCSVFormat
      import fs2.text

      import cats.implicits._
      import doobie.implicits._
      import doobie.util.update.Update

      val maxRowsToCommit = 100

      import com.github.gekomad.ittocsv.core.FromCsv._

      implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default

      //read from file N rows and store in db
      def readCsvAndWriteDB[B: doobie.util.Read: doobie.util.Write: FieldNames: Schema](
        inOutFile: String,
        sql: String
      ): IO[Unit] = {
        import cats.instances.list._
        import cats.syntax.traverse._
        def bulkInsert[A: doobie.util.Read: doobie.util.Write: FieldNames: Schema](
          csvList: List[String],
          count: Long
        ): Either[NonEmptyList[ParseFailure], IO[Int]] = {
          val csv =
            if (count == 0)
              csvList.drop(1)
            else csvList

          //write the list into db
          val x: Either[NonEmptyList[ParseFailure], List[A]] = csv.map(fromCsv[A](_).head).sequence

          x match {
            case Right(t) =>
              val dd = doobierecipes.Transactor.transactor.use { xa =>
                Update[A](sql).updateMany(t).transact(xa)
              }
              Right(dd)
            case Left(e) =>
              println("err: " + e)
              Left(e)
          }
        }

        val a = Files[IO]
          .readAll(fs2.io.file.Path(inOutFile))
          .through(text.utf8.decode)
          .through(text.lines)
          .chunkN(maxRowsToCommit)
          .zipWithIndex
          .map(chunk => bulkInsert[B](chunk._1.toList, chunk._2).map(_.unsafeRunSync()))

        a.compile.drain

      }
    }

    import java.nio.file.Paths
    import com.github.gekomad.ittocsv.parser.IttoCSVFormat
    import doobierecipes.RandomUtil._

    import cats.implicits._
    import doobie.implicits._

    val nRecords  = 1000
    val inOutFile = s"$tmpDir/test2.csv"

    import com.github.gekomad.ittocsv.core.Header.csvHeader
    import com.github.gekomad.ittocsv.core.ToCsv._
    implicit val csvFormat: IttoCSVFormat = IttoCSVFormat.default

    def tableTest2: List[Test2] =
      doobierecipes.Transactor.transactor
        .use { xa =>
          sql"select field1, field2, field3 from test2 order by 1"
            .query[Test2]
            .to[List]
            .transact(xa)
        }
        .unsafeRunSync()

    //create csv file
    val test2List = (0 until nRecords).toList.map { count =>
      Test2(count, getRandomOptionString(10 + getRandomInt(100)), getRandomOptionInt(2000))
    }

    val content =
      test2List.foldLeft(csvHeader[Test2])((a, b) => a + IttoCSVFormat.default.recordSeparator + toCsv(b)).getBytes

    java.nio.file.Files.write(Paths.get(inOutFile), content)

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

      doobierecipes.Transactor.transactor
        .use { xa =>
          (drop *> create).transact(xa)
        }
        .unsafeRunSync()
    })

    assert(tableTest2 == Nil)

    ReadCsvAndWriteDB
      .readCsvAndWriteDB[Test2](inOutFile, "insert into test2 (field1, field2, field3) values (?, ?, ?)")
      .unsafeRunSync()

    assert(tableTest2 == test2List)
    assert(tableTest2.size == nRecords)
  }

}
