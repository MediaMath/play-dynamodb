/**
 *  Copyright (C) 2013-2014 MediaMath <http://www.mediamath.com>
 *
 * @author ihummel
 */
package play.api.libs.dynamodb

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => the, any}
import com.amazonaws.services.dynamodbv2.model.AttributeValue


class ReadsSpec extends FlatSpec with ShouldMatchers with MockitoSugar {

	"Reads" should "work with traversables" in {
		val attr = new AttributeValue().withSS("homer", "marge", "lisa", "bart", "maggie")
		val item = Item.parse(Map("friends" -> attr))

		val reads = DdbKey("friends").read[List[String]]
		reads.reads(item) should equal(DdbSuccess(List("homer", "marge", "lisa", "bart", "maggie")))
	}
}
