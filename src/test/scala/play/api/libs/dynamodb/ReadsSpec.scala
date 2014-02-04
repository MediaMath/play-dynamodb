/**
 *  Copyright (C) 2013-2014 MediaMath <http://www.mediamath.com>
 *
 * @author ihummel
 */
package play.api.libs.dynamodb

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.amazonaws.services.dynamodbv2.model.AttributeValue


class ReadsSpec extends FlatSpec with ShouldMatchers {
  "Reads" should "work with traversables" in {
    val attr = new AttributeValue().withSS("homer", "marge", "lisa", "bart", "maggie")
    val item = Item.parse(Map("friends" -> attr))

    val reads = DdbKey("friends").read[List[String]]
    reads.reads(item) should equal(DdbSuccess(List("homer", "marge", "lisa", "bart", "maggie")))
  }

  "Reads" should "fail with wrong type traversables" in {
    val attr = new AttributeValue().withSS("homer", "marge")
    val item = Item.parse(Map("friends" -> attr))

    val reads = DdbKey("friends").read[List[Int]]
    reads.reads(item) should equal(DdbError(List("error.expected.ddbnumber")))
  }

  "Reads" should "fail with wrong type traversables (2)" in {
    val attr = new AttributeValue().withS("homer")
    val item = Item.parse(Map("friends" -> attr))

    val reads = DdbKey("friends").read[List[String]]
    reads.reads(item) should equal(DdbError(List("error.expected.ddbset")))
  }

  "Reads" should "work with BigDecimal" in {
    val bigDecimal = BigDecimal("123.456")
    val attr = new AttributeValue().withN(bigDecimal.toString())
    val item = Item.parse(Map("number" -> attr))

    val reads = DdbKey("number").read[BigDecimal]
    reads.reads(item) should equal(DdbSuccess(bigDecimal))
  }

  "Reads" should "work with BigDecimal Set" in {
    val bigDecimal1 = BigDecimal("123.4567")
    val bigDecimal2 = BigDecimal("123")
    val bigDecimal3 = BigDecimal("0.4567")
    val attr = new AttributeValue().withNS(bigDecimal1.toString(), bigDecimal2.toString(), bigDecimal3.toString())
    val item = Item.parse(Map("numbers" -> attr))

    val reads = DdbKey("numbers").read[List[BigDecimal]]
    reads.reads(item) should equal(DdbSuccess(List(bigDecimal1, bigDecimal2, bigDecimal3)))
  }

  "Reads" should "invalidate String as BigDecimal" in {
    val attr = new AttributeValue().withS("string")
    val item = Item.parse(Map("number" -> attr))

    val reads = DdbKey("number").read[BigDecimal]
    reads.reads(item) should equal(DdbError(List("error.expected.ddbnumber")))
  }

}
