/**
 * Copyright (C) 2013-2014 MediaMath <http://www.mediamath.com>
 *
 * @author ihummel
 */
package play.api.libs.dynamodb


case class DdbKey(key: String) {
  def read[T](implicit r: Reads[T]): Reads[T] = Reads.at[T](this)(r)
  def findWithin(item: DdbItem): DdbResult[DdbValue] = {
    item.value.get(key) match {
      case Some(value) => DdbSuccess(value)
      case None => DdbError(Seq("error.path.missing"))
    }
  }
}
