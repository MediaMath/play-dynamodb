name := "play-dynamodb"

version := "0.2-SNAPSHOT"

organization := "com.mediamath.play"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-encoding", "utf-8", "-deprecation", "-unchecked", "-feature")

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies +=  "com.typesafe.play" %% "play-json" % "2.2.1"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.6.9.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"


s3credentials := Some((sys.env("AWS_ACCESS_KEY_ID"), sys.env("AWS_SECRET_ACCESS_KEY")))


publishMavenStyle := false

publishTo <<= (isSnapshot, s3credentials) { (snapshot, credentials) =>
    val suffix = if (snapshot) "snapshots" else "releases"
    credentials map S3Resolver("MM " + suffix + " bucket", "s3://mm-repo-" + suffix, Resolver.ivyStylePatterns, true).toSbtResolver
}


