import cats.effect.IO
import doobie.implicits._
import doobierecipes.Transactor._
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global
import cats.~>
import doobie.WeakAsync
import doobie.free.connection.ConnectionIO
import scala.collection.immutable

class SelectMultipleColumns extends AnyFunSuite {

  test("IO to ConnectionIO") {
    val x: IO[Int] = IO(1)
    val res = WeakAsync.liftK[IO, ConnectionIO].use { toConnectionIO: (IO ~> ConnectionIO) =>
      val aa: ConnectionIO[Int] = toConnectionIO(x)
      val p: IO[Int]            = transactor.use(xa => aa.transact(xa))
      p
    }
    val aa = res.unsafeRunSync()
    println(aa)
  }

  test("select multiple columns") {

    val mySelect: immutable.Seq[(String, String, Int, Option[Double])] = transactor
      .use { xa =>
        sql"select code, name, population, gnp from country"
          .query[(String, String, Int, Option[Double])]
          .to[List] // ConnectionIO[List[(String, String, Int, Option[Double])]]
          .transact(xa) // IO[List[(String, String, Int, Option[Double])]]
      }
      .unsafeRunSync() // List[(String, String, Int, Option[Double])]
      .take(3) // List[(String, String, Int, Option[Double])]

    assert(
      mySelect == List(
        ("AFG", "Afghanistan", 22720000, Some(5976.0)),
        ("NLD", "Netherlands", 15864000, Some(371362.0)),
        ("ANT", "Netherlands Antilles", 217000, Some(1941.0))
      )
    )
  }

}
