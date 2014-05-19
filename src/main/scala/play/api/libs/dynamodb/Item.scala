/**
 * Copyright (C) 2014 MediaMath <http://www.mediamath.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author themodernlife
 * @author alekseyig
 */
package play.api.libs.dynamodb

import java.nio.ByteBuffer

import scala.collection.JavaConversions._

import com.amazonaws.services.dynamodbv2.model.AttributeValue

object Item {
  def parse(m: Map[String, AttributeValue]): DdbItem = {
    val values = m.map { case (k, value) =>
      k -> (value match {
        case v if v.getN != null  => DdbNumber(BigDecimal(v.getN))
        case v if v.getS != null  => DdbString(v.getS)
        case v if v.getB != null  => DdbBinary(v.getB.array())
        case v if v.getNS != null => DdbSet(v.getNS.toSet.map { s: String => DdbNumber(BigDecimal(s)) })
        case v if v.getSS != null => DdbSet(v.getSS.toSet.map { s: String => DdbString(s) })
        case v if v.getBS != null => DdbSet(v.getBS.toSet.map { b: ByteBuffer => DdbBinary(b.array()) })
        case _ => throw new IllegalStateException("Couldn't match item: " + value)
      })
    }.toMap

    DdbItem(values)
  }
}