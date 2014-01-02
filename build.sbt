

name := "play-dynamodb"

version := "0.1-SNAPSHOT"

organization := "com.mediamath.play"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-encoding", "utf-8", "-deprecation", "-unchecked", "-feature")

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies +=  "com.typesafe.play" %% "play-json" % "2.2.0-RC1"

libraryDependencies += "com.amazonaws"      % "aws-java-sdk"       % "1.5.4"




libraryDependencies += "org.scalatest"     %% "scalatest"       % "1.9.1" % "test"

libraryDependencies += "org.mockito"        % "mockito-all"     % "1.9.5" % "test"





s3credentials := Some((sys.env("AWS_ACCESS_KEY_ID"), sys.env("AWS_SECRET_ACCESS_KEY")))




publishMavenStyle := false

publishTo <<= (isSnapshot, s3credentials) { (snapshot, credentials) =>
    val suffix = if (snapshot) "snapshots" else "releases"
    credentials map S3Resolver("MM " + suffix + " bucket", "s3://mm-repo-" + suffix, Resolver.ivyStylePatterns, true).toSbtResolver
}


