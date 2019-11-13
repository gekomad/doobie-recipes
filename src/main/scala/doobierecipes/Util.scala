package doobierecipes

import java.io.{File, PrintWriter}
import cats.effect.IO
import doobierecipes.Transactor._
import scala.language.reflectiveCalls
import com.github.gekomad.ittocsv.core.Header.{FieldNames, csvHeader}
import com.github.gekomad.ittocsv.parser.IttoCSVFormat
import cats.implicits._
import doobie.implicits._

object Util {
  val tmpDir: String = System.getProperty("java.io.tmpdir")

  def deleteFile(filename: String): Boolean = { new File(filename).delete() }

  def autoclose[A <: { def close(): Unit }, B](resource: IO[A])(f: A => IO[B]): IO[B] =
    resource.bracket(f) { closable =>
      IO(closable.close())
        .handleErrorWith(_ => IO.unit)
        .void
    }

  def writeIttoHeaderTofile[A: FieldNames](fileName: String)(implicit csvFormat: IttoCSVFormat): IO[Unit] =
    autoclose(IO(new PrintWriter(new File(fileName)))) { fis =>
      fis.write(csvHeader[A])
      IO.unit
    }

  def dropCreateTableTableEnum(): Int = {
    val drop = sql"""DROP TABLE IF EXISTS table_enum""".update.run

    val create =
      sql"""CREATE TABLE table_enum (
           | id   int,
           | product_type VARCHAR NOT NULL)""".stripMargin.update.run

    transactor.use { xa =>
      (drop *> create).transact(xa)
    }.unsafeRunSync
  }

  def dropCreateTablePerson(): Int = {
    val drop = sql"""DROP TABLE IF EXISTS person""".update.run

    val create =
      sql""" 
           | CREATE TABLE person (
           | id   SERIAL,
           | name VARCHAR NOT NULL UNIQUE,
           | age  SMALLINT)
           | """.stripMargin.update.run

    transactor.use { xa =>
      (drop *> create).transact(xa)
    }.unsafeRunSync
  }

  def dropCreateTablePersonPets(): Int = {
    val drop = sql"DROP TABLE IF EXISTS person_pets".update.run

    val create =
      sql"""
           | CREATE TABLE person_pets (
           | id SERIAL,
           | name VARCHAR   NOT NULL UNIQUE,
           | pets VARCHAR[] NOT NULL)
           | """.stripMargin.update.run
    transactor.use { xa =>
      (drop *> create).transact(xa)
    }.unsafeRunSync
  }
}
