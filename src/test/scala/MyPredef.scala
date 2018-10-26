import cats.effect.IO
import doobie.Transactor

object MyPredef {

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
  implicit val cs = IO.contextShift(ExecutionContext.global)


  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO] // our transaction EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver", // driver classname
        "jdbc:postgresql:world", // connect URL
        "postgres", // username
        "pass1", // password
        ce, // await connection here
        te // execute JDBC operations here
      )
    } yield xa

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
