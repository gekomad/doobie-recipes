package ddl

import doobierecipes.Transactor._
import doobierecipes.Util._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.unsafe.implicits.global

class Vacuum extends AnyFunSuite with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    dropCreateTablePerson().unsafeRunSync()
    ()
  }

  test("vacuum") {

    import doobie.implicits._
    import doobie.FC
    import cats.implicits._

    val vacuum = FC.setAutoCommit(true) *> sql"vacuum full person".update.run *> FC.setAutoCommit(false)

    transactor
      .use(xa => vacuum.transact(xa))
      .attempt
      .map {
        case Left(e)  => assert(false, e)
        case Right(_) => assert(true)
      }
      .unsafeRunSync()

  }

}
