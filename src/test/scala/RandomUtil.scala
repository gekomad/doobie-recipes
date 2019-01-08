import java.sql.Timestamp
import java.time.LocalDate
import java.util.UUID

import scala.util.Random

object RandomUtil {

  val extensions = List(".com", ".it", ".eu", ".info", ".fr", ".co.uk")

  def getRandomUUID: UUID = UUID.randomUUID

  def getRandomString(lung: Int): String = scala.util.Random.alphanumeric.take(lung).mkString

  def getRandomOptionString(lung: Int): Option[String] = if (getRandomBoolean) None else Some(scala.util.Random.alphanumeric.take(lung).mkString.replace("a", ",").replace("e", "\""))

  def getRandomInt(from: Int, until: Int): Int = Random.shuffle(from to until).take(1).head

  def getRandomOptionInt(from: Int, until: Int): Option[Int] = if (getRandomBoolean) None else Some(Random.shuffle(from to until).take(1).head)

  def getRandomStringList(a: Int, b: Int): List[String] = (1 to getRandomInt(a) + 1).map(_ => getRandomString(b)).toList

  def getRandomInt(until: Int): Int = getRandomInt(0, until)

  def getRandomLocalDate: LocalDate = LocalDate.of(getRandomInt(1973, 2018), getRandomInt(1, 12), getRandomInt(1, 28))

  def getRandomLong: Long = scala.util.Random.nextLong()

  def getRandomFloat: Float = scala.util.Random.nextFloat()

  def getRandomBoolean: Boolean = Random.nextBoolean

  def getRandomUrl: String = "http://www." + (getRandomString(getRandomInt(10) + 3) + extensions(getRandomInt(extensions.length - 1))).toLowerCase

  def getRandomBigString(l: Int = 100): String = getRandomString(l).replace('k', ' ').replace('z', ' ').replace('j', ' ')

  def getRandomTimestamp: Timestamp = {
    val unixtime = 1293861599 + scala.util.Random.nextDouble() * 60 * 60 * 24 * 365
    val o        = new java.util.Date(unixtime.toLong).getTime
    new Timestamp(o)
  }

}
