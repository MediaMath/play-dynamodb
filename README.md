
```scala
trait Monoids {
	implicit val hllMonoid = new HyperLogLogMonoid(12)
}

trait AlgebirdReads { self: Monoids =>
	implicit val hllReads = new Reads[HLL] {
		def reads(item: DdbValue) = item match {
			case DdbBinary(n) => DdbSuccess(self.hllMonoid(n))
			case _ => DdbError(Seq("error.expected.ddbbinary"))
		}
	}
}

case class HourlyPixelMetric(pixel: Long, dayHour: String, hll: HLL, loads: Long)

object DynamodbTest extends App with Monoids with AlgebirdReads {

	val dynamodb = new AmazonDynamoDBClient()

	val hashKey = new AttributeValue().withN("356601")
	val request1 = new GetItemRequest().withTableName("pixel-stats-prod").withKey(Map("pixel" -> hashKey, "dayhour" -> new AttributeValue().withS("2013-11-10-00")))
	val request2 = new GetItemRequest().withTableName("pixel-stats-prod").withKey(Map("pixel" -> hashKey, "dayhour" -> new AttributeValue().withS("2013-11-10-01")))
	val output1 = dynamodb.getItem(request1)
	val output2 = dynamodb.getItem(request2)


	implicit val hourlyPixelMetricReads = (
		DdbKey("pixel").read[Long] and
		DdbKey("dayhour").read[String] and
		DdbKey("hll").read[HLL] and
		DdbKey("loads").read[Long]
	)(HourlyPixelMetric)

	val item = Item.parse(output1)

	val good = DdbKey("pixel").read[Long].reads(item)
	println("good: " + good)


	val wrongtype = DdbKey("pixel").read[String].reads(item)
	println("wrongtype:" + wrongtype)

	val nonexistant = DdbKey("non-existant").read[String].reads(item)
	println("nonexistant: " + nonexistant)



	val hpm1 = Item.parse(output1).validate[HourlyPixelMetric]
	println("hmp? " + hpm1)

	val hpm2 = Item.parse(output2).validate[HourlyPixelMetric]
	println("hmp? " + hpm2)

	//validate returns a monad (DdbResult)
	for (h1 <- hpm1; h2 <- hpm2) {
		val added = h1.hll + h2.hll
		println("Added: " + added)
	}
}
```