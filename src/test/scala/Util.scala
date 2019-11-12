import java.io.{File, PrintWriter}
import cats.effect.IO
import cats.implicits._
import com.github.gekomad.ittocsv.core.Header.{FieldNames, csvHeader}
import com.github.gekomad.ittocsv.parser.IttoCSVFormat

import scala.language.reflectiveCalls

object Util {

  def deleteFile(filename: String) = { new File(filename).delete() }

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

  val tmpDir: String = System.getProperty("java.io.tmpdir")

  import scala.concurrent.ExecutionContext

  import doobie.implicits._
  import cats.implicits._
  import cats.effect._
  import cats.implicits._
  import doobie._
  import doobie.implicits._
  import doobie.hikari._

  // We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
  // is where nonblocking operations will be executed.
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- Blocker[IO]                               // our transaction EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver", // driver classname
        "jdbc:postgresql://localhost:5435/world", // connect URL
        "postgres", // username
        "", // password
        ce, // await connection here
        te // execute JDBC operations here
      )
    } yield xa

  def createTableTableEnum: Int = {
    val drop = sql"""DROP TABLE IF EXISTS table_enum""".update.run

    val create =
      sql"""
        CREATE TABLE table_enum (
        id   int,
        product_type VARCHAR NOT NULL        
        )
      """.update.run

    transactor.use { xa =>
      (drop *> create).transact(xa)
    }.unsafeRunSync
  }

  def createTablePerson: Int = {
    val drop = sql"""DROP TABLE IF EXISTS person""".update.run

    val create =
      sql"""
        CREATE TABLE person (
        id   SERIAL,
        name VARCHAR NOT NULL UNIQUE,
        age  SMALLINT
        )
      """.update.run

    transactor.use { xa =>
      (drop *> create).transact(xa)
    }.unsafeRunSync
  }

  def createTablePersonPets: Int = {
    val drop = sql"DROP TABLE IF EXISTS person_pets".update.run

    val create =
      sql"""
        CREATE TABLE person_pets (
        id   SERIAL,
        name VARCHAR   NOT NULL UNIQUE,
        pets VARCHAR[] NOT NULL
      )
    """.update.run
    transactor.use { xa =>
      (drop *> create).transact(xa)
    }.unsafeRunSync
  }
}

import java.util.UUID
import scala.util.Random

object RandomUtil {

  def getRandomUUID: UUID = UUID.randomUUID

  def getRandomOptionString(lung: Int): Option[String] =
    if (getRandomBoolean) None
    else Some(scala.util.Random.alphanumeric.take(lung).mkString.replace("a", ",").replace("e", "\""))

  def getRandomInt(from: Int, until: Int): Int = Random.shuffle(from to until).take(1).head

  def getRandomOptionInt(from: Int, until: Int): Option[Int] =
    if (getRandomBoolean) None else Some(Random.shuffle(from to until).take(1).head)

  def getRandomInt(until: Int): Int = getRandomInt(0, until)

  def getRandomBoolean: Boolean = Random.nextBoolean

}
