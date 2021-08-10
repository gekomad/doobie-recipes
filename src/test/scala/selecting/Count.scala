import doobie.implicits._
import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global

class Count extends AnyFunSuite {

  test("select count") {
    val mySelect = transactor.use { xa =>
      sql"select count(1) from country"
        .query[Int]
        .unique
        .transact(xa)
    }

    assert(mySelect.unsafeRunSync() == 239)
  }

}
