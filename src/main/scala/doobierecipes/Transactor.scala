package doobierecipes

import cats.effect.IO
import cats.effect._
import doobie._
import doobie.hikari._
import scala.concurrent.ExecutionContext

object Transactor {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](8) // our connect EC
      te <- Blocker[IO]                              // our transaction EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver", // driver classname
        "jdbc:postgresql://localhost:5435/world", // connect URL
        "postgres", // username
        "", // password
        ce, // await connection here
        te // execute JDBC operations here
      )
    } yield xa

}
