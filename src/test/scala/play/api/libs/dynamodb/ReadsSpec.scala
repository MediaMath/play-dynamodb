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

import org.specs2.mutable._
import com.amazonaws.services.dynamodbv2.model.AttributeValue

class ReadsSpec extends Specification {
  "Reads" should {
    "work with traversables" in {
      val attr = new AttributeValue().withSS("homer", "marge", "lisa", "bart", "maggie")
      val item = Item.parse(Map("friends" -> attr))

      val reads = DdbKey("friends").read[List[String]]
      reads.reads(item) should be_==(DdbSuccess(List("homer", "marge", "lisa", "bart", "maggie")))
    }

    "fail with wrong type traversables" in {
      val attr = new AttributeValue().withSS("homer", "marge")
      val item = Item.parse(Map("friends" -> attr))

      val reads = DdbKey("friends").read[List[Int]]
      reads.reads(item) should be_==(DdbError(List("error.expected.ddbnumber")))
    }

    "fail with wrong type traversables (2)" in {
      val attr = new AttributeValue().withS("homer")
      val item = Item.parse(Map("friends" -> attr))

      val reads = DdbKey("friends").read[List[String]]
      reads.reads(item) should be_==(DdbError(List("error.expected.ddbset")))
    }

    "work with BigDecimal" in {
      val bigDecimal = BigDecimal("123.456")
      val attr = new AttributeValue().withN(bigDecimal.toString())
      val item = Item.parse(Map("number" -> attr))

      val reads = DdbKey("number").read[BigDecimal]
      reads.reads(item) should be_==(DdbSuccess(bigDecimal))
    }

    "work with BigDecimal Set" in {
      val bigDecimal1 = BigDecimal("123.4567")
      val bigDecimal2 = BigDecimal("123")
      val bigDecimal3 = BigDecimal("0.4567")
      val attr = new AttributeValue().withNS(bigDecimal1.toString(), bigDecimal2.toString(), bigDecimal3.toString())
      val item = Item.parse(Map("numbers" -> attr))

      val reads = DdbKey("numbers").read[List[BigDecimal]]
      reads.reads(item) should be_==(DdbSuccess(List(bigDecimal1, bigDecimal2, bigDecimal3)))
    }

    "invalidate String as BigDecimal" in {
      val attr = new AttributeValue().withS("string")
      val item = Item.parse(Map("number" -> attr))

      val reads = DdbKey("number").read[BigDecimal]
      reads.reads(item) should be_==(DdbError(List("error.expected.ddbnumber")))
    }

    "work with Double" in {
      val double:Double = 1.2D
      val attr = new AttributeValue().withN(double.toString)
      val item = Item.parse(Map("number" -> attr))

      val reads = DdbKey("number").read[Double]
      reads.reads(item) should be_==(DdbSuccess(double))
    }

    "invalidate String as Double" in {
      val attr = new AttributeValue().withS("string")
      val item = Item.parse(Map("number" -> attr))

      val reads = DdbKey("number").read[Double]
      reads.reads(item) should be_==(DdbError(List("error.expected.ddbnumber")))
    }
  }
}
