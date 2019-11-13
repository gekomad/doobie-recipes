package doobierecipes

import scala.util.Random

object RandomUtil {

  def getRandomInt(until: Int): Int = Random.nextInt(until)

  def getRandomBoolean: Boolean = Random.nextBoolean

  def getRandomOptionString(lung: Int): Option[String] =
    if (getRandomBoolean) None else Some(scala.util.Random.alphanumeric.take(lung).mkString)

  def getRandomOptionInt(until: Int): Option[Int] = if (getRandomBoolean) None else Some(getRandomInt(until))

}
