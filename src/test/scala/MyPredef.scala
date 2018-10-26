import cats.effect.IO
import doobie.Transactor

object MyPredef {
  import scala.concurrent.ExecutionContext

  import MyPredef.xa
  import doobie.implicits._
  import cats.implicits._

  // We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
  // is where nonblocking operations will be executed.
  implicit val cs = IO.contextShift(ExecutionContext.global)

  // A transactor that gets connections from java.sql.DriverManager and excutes blocking operations
  // on an unbounded pool of daemon threads. See the chapter on connection handling for more info.
  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:world", // connect URL (driver-specific)
    "postgres", // user
    "pass1" // password
  )

  def createTable: Int = {
    val drop = sql"""DROP TABLE IF EXISTS person""".update.run

    val create =
      sql"""
        CREATE TABLE person (
        id   SERIAL,
        name VARCHAR NOT NULL UNIQUE,
        age  SMALLINT
        )
      """.update.run

    (drop, create).mapN(_ + _).transact(xa).unsafeRunSync

  }
}
