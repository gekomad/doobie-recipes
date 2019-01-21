import java.io.{File, PrintWriter}

import cats.effect.IO
import cats.implicits._
import com.github.gekomad.ittocsv.core.Header.{FieldNames, csvHeader}
import com.github.gekomad.ittocsv.parser.IttoCSVFormat

import scala.language.reflectiveCalls

object Util {

  def autoclose[A <: { def close(): Unit }, B](resource: IO[A])(f: A => IO[B]): IO[B] =
    resource.bracket(f) { closable =>
      IO(closable.close())
        .handleErrorWith(_ => IO.unit)
        .void
    }

  def writeIttoHeaderTofile[A: FieldNames](fileName: String)(implicit csvFormat: IttoCSVFormat): IO[Unit] =
    autoclose(IO(new PrintWriter(new File(fileName)))) { fis =>
      fis.write(csvHeader[A])
      IO.unit
    }

}
