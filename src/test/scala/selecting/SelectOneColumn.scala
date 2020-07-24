import cats.effect._
import doobie.implicits._
import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite

class SelectOneColumn extends AnyFunSuite {

  test("select one column") {
    val mySelect: IO[List[String]] = transactor.use { xa =>
      sql"select name from country"
        .query[String]
        .to[List]
        .transact(xa)
    }

    assert(mySelect.unsafeRunSync().take(3) == List("Afghanistan", "Netherlands", "Netherlands Antilles"))
  }

}
