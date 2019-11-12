import java.io.{File, PrintWriter}
import cats.effect.IO
import cats.implicits._
import com.github.gekomad.ittocsv.core.Header.{FieldNames, csvHeader}
import com.github.gekomad.ittocsv.parser.IttoCSVFormat
import doobie.Transactor

import scala.language.reflectiveCalls

object Util {

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
      (drop, create).mapN(_ + _).transact(xa)
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
      (drop, create).mapN(_ + _).transact(xa)
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
      (drop, create).mapN(_ + _).transact(xa)
    }.unsafeRunSync
  }
}

import java.sql.Timestamp
import java.time.LocalDate
import java.util.UUID

import scala.util.Random

object RandomUtil {

  val extensions: List[String] = List(".com", ".it", ".eu", ".info", ".fr", ".co.uk")

  def getRandomUUID: UUID = UUID.randomUUID

  def getRandomString(lung: Int): String = scala.util.Random.alphanumeric.take(lung).mkString

  def getRandomOptionString(lung: Int): Option[String] =
    if (getRandomBoolean) None
    else Some(scala.util.Random.alphanumeric.take(lung).mkString.replace("a", ",").replace("e", "\""))

  def getRandomInt(from: Int, until: Int): Int = Random.shuffle(from to until).take(1).head

  def getRandomOptionInt(from: Int, until: Int): Option[Int] =
    if (getRandomBoolean) None else Some(Random.shuffle(from to until).take(1).head)

  def getRandomStringList(a: Int, b: Int): List[String] = (1 to getRandomInt(a) + 1).map(_ => getRandomString(b)).toList

  def getRandomInt(until: Int): Int = getRandomInt(0, until)

  def getRandomLocalDate: LocalDate = LocalDate.of(getRandomInt(1973, 2018), getRandomInt(1, 12), getRandomInt(1, 28))

  def getRandomLong: Long = scala.util.Random.nextLong()

  def getRandomFloat: Float = scala.util.Random.nextFloat()

  def getRandomBoolean: Boolean = Random.nextBoolean

  def getRandomUrl: String =
    "http://www." + (getRandomString(getRandomInt(10) + 3) + extensions(getRandomInt(extensions.length - 1))).toLowerCase

  def getRandomBigString(l: Int = 100): String =
    getRandomString(l).replace('k', ' ').replace('z', ' ').replace('j', ' ')

  def getRandomTimestamp: Timestamp = {
    val unixtime = 1293861599 + scala.util.Random.nextDouble() * 60 * 60 * 24 * 365
    val o        = new java.util.Date(unixtime.toLong).getTime
    new Timestamp(o)
  }

}
