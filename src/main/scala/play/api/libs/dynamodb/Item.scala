/**
 * Copyright (C) 2013-2014 MediaMath <http://www.mediamath.com>
 *
 * @author ihummel
 */
package play.api.libs.dynamodb

import com.amazonaws.services.dynamodbv2.model.GetItemResult
import scala.collection.JavaConversions._
import java.nio.ByteBuffer


object Item {
  def parse(g: GetItemResult): DdbItem = {
    val values = g.getItem.map { case (k, v) =>
      k -> (v match {
        case v if v.getN != null  => DdbNumber(BigDecimal(v.getN))
        case v if v.getS != null  => DdbString(v.getS)
        case v if v.getB != null  => DdbBinary(v.getB.array())
        case v if v.getNS != null => DdbSet(v.getNS.toSet.map { s: String => DdbNumber(BigDecimal(s)) })
        case v if v.getSS != null => DdbSet(v.getSS.toSet.map { s: String => DdbString(s) })
        case v if v.getBS != null => DdbSet(v.getBS.toSet.map { b: ByteBuffer => DdbBinary(b.array()) })
        case _ => throw new IllegalStateException("Couldn't match item: " + v)
      })
    }.toMap

    DdbItem(values)
  }
}