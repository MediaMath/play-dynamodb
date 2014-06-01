
Play-DynamoDB
=============

Play-DynamoDB is an API for validating DynamoDB records and converting them into Scala case classes.  It's inspired
by the [JSON combinators](http://www.playframework.com/documentation/2.2.x/ScalaJsonCombinators) available in the core Play distribution.


Current version: `0.4.0-SNAPSHOT`


Getting Play-DynamoDB
---------------------

Play DynamoDB is hosted via Sonatype OSS.

Add the following to your `build.sbt` file:

```scala
resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += "com.mediamath.play" %% "play-dynamodb" % "0.4.1"
```

Features
--------

- Easily convert data stored in DynamoDB into case classes
- Supports primitive types, iterables, byte arrays and Joda-Time classes


Usage
-----

First, make sure you understand the [JSON combinators API](http://www.playframework.com/documentation/2.2.x/ScalaJsonCombinators).
Then you just need to create an implicit reads for your case class and call 'validate'.  You will get back either
a `DdbSuccess` or a `DdbError` depending on whether or not the validation/parsing succeeded.

```scala
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
```


Authors
-------

- Ian Hummel https://github.com/themodernlife
- Aleksey Zhukov https://github.com/alekseyig