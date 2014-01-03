/**
 * Copyright (C) 2013-2014 MediaMath <http://www.mediamath.com>
 *
 * @author ihummel
 */
package play.api.libs.dynamodb

sealed trait DdbValue {
  def validate[T](implicit rds: Reads[T]): DdbResult[T] = rds.reads(this)
}
case class DdbItem(value: Map[String, DdbValue]) extends DdbValue
case class DdbNumber(value: BigDecimal) extends DdbValue
case class DdbString(value: String) extends DdbValue
case class DdbBinary(value: Array[Byte]) extends DdbValue
case class DdbSet(value: Set[DdbValue] = Set()) extends DdbValue
