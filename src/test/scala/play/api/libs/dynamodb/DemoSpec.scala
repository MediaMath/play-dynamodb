/**
 * Copyright (C) 2014 MediaMath <http://www.mediamath.com>
 *
 * @author ihummel
 */
package demo

import org.specs2.mutable._
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import java.nio.ByteBuffer
import org.joda.time.DateTimeZone

class DemoSpec extends Specification {
  "A Demo" should {
    "demonstrate usage" in {

      import play.api.libs.functional.syntax._
      import play.api.libs.dynamodb._
      import org.joda.time.DateTime

      /**
       * Example item as returned from DynamoDB API
       */
      val item = Map(
        "username"       → new AttributeValue().withS("themodernlife"),
        "favoriteQuotes" → new AttributeValue().withSS("Audentes fortuna iuvat", "Festina lente"),
        "githubUrl"      → new AttributeValue().withS("https://github.com/themodernlife"),
        "commits"        → new AttributeValue().withN("25"),
        "createdAt"      → new AttributeValue().withS("2014-05-19 11:26:00")
      )

      /**
       * Case class for objects stored in DynamoDB
       */
      case class User(username: String, favoriteQuotes: Set[String], githubUrl: Option[String], commits: Int, createdAt: DateTime)

      /**
       * Override default date parsing
       */
      implicit val jodaDateTimeReads = Reads.dateTimeReads("yyyy-MM-dd HH:mm:ss")

      /**
       * Formula for validating a User case class
       */
      implicit val userReads = (
        DdbKey("username").read[String] and
        DdbKey("favoriteQuotes").read[Set[String]] and
        DdbKey("githubUrl").read[Option[String]] and
        DdbKey("commits").read[Int] and
        DdbKey("createdAt").read[DateTime]
      )(User)

      /**
       * Perform the validation and convert to case class
       */
      val user = Item.parse(item).validate[User]

      val expected = DdbSuccess(
        User(
          "themodernlife",
          Set("Audentes fortuna iuvat", "Festina lente"),
          Some("https://github.com/themodernlife"),
          25,
          new DateTime(2014, 5, 19, 11, 26, 0)
        )
      )
      user must be_==(expected)
    }
  }
}
