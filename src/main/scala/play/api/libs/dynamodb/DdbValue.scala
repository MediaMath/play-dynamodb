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

sealed trait DdbValue {
  def validate[T](implicit rds: Reads[T]): DdbResult[T] = rds.reads(this)
}

case class DdbItem(value: Map[String, DdbValue]) extends DdbValue
case class DdbNumber(value: BigDecimal) extends DdbValue
case class DdbString(value: String) extends DdbValue
case class DdbBinary(value: Array[Byte]) extends DdbValue
case class DdbSet(value: Set[DdbValue] = Set()) extends DdbValue
