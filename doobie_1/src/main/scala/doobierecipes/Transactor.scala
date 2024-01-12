package doobierecipes

import cats.effect.IO
import cats.effect._
import doobie._
import doobie.hikari._

object Transactor {

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](8)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver", // driver classname
        "jdbc:postgresql://localhost:5435/world",
        "postgres", // username
        "postgres", // password
        ce
      )
    } yield xa

}
