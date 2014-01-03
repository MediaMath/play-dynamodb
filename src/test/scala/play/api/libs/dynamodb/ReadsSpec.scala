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
}
