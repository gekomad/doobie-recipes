import doobierecipes.Transactor._
import doobie.implicits._
import org.scalatest.funsuite.AnyFunSuite
import doobie.util.{Get, Put}
import doobierecipes.Util._
import org.scalatest.BeforeAndAfterAll

class Enum extends AnyFunSuite with BeforeAndAfterAll {

  /**
    * CREATE TABLE table_enum (
    * id   int,
    * product_type VARCHAR NOT NULL)
    */
  override def beforeAll(): Unit = dropCreateTableTableEnum().unsafeRunSync()

  object ProductType extends Enumeration {
    type ProductType = Value

    val FOO, BAR = Value

    def fromString1(s: String): ProductType.Value = values.find(_.toString == s).getOrElse {
      throw new Exception(s"error can't decode $s")
    }

    def toString1(e: ProductType.Value): String = e.toString

    implicit val natGet: Get[ProductType.Value] = Get[String].map(ProductType.fromString1)
    implicit val natPut: Put[ProductType.Value] = Put[String].contramap(ProductType.toString1)
  }

  case class TableEnum(id: Int, productType: ProductType.Value)

  test("insert and read enum") {

    //insert
    import doobie.util.update.Update0
    def insert1(id: Int, productType: ProductType.Value): Update0 =
      sql"insert into table_enum (id, product_type) values ($id, $productType)".update

    assert(
      transactor
        .use { xa =>
          insert1(1, ProductType.FOO).run.transact(xa)
        }
        .unsafeRunSync() == 1
    )

    //read
    {
      val mySelect = transactor
        .use { xa =>
          sql"select id, product_type from table_enum"
            .query[TableEnum]
            .to[List]
            .transact(xa)
        }
        .unsafeRunSync()

      assert(mySelect == List(TableEnum(1, ProductType.FOO)))
    }

  }
}
