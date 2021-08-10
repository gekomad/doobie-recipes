package selecting

import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite
import java.time.LocalDateTime
import doobie.implicits._
import cats.implicits._
import doobie.implicits.javatimedrivernative._

class Timestamp extends AnyFunSuite {

  test("select timestamp column") {

    val now    = LocalDateTime.now()
    val drop   = sql"DROP TABLE IF EXISTS foo".update.run
    val create = sql"CREATE TABLE foo(id SERIAL, date timestamp)".update.run
    val insert = sql"insert into foo (id,date) values (1,$now)".update.run
    val select = sql"select date from foo limit 1".query[LocalDateTime].option
    val date   = transactor.use(xa => (drop *> create *> insert *> select).transact(xa)).unsafeRunSync()

    assert(date.head == now)

  }
}
