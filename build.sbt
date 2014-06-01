name         := "play-dynamodb"

organization := "com.mediamath.play"

scalaVersion := "2.10.3"


scalacOptions ++= Seq("-encoding", "utf-8", "-deprecation", "-unchecked", "-feature")


resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies += "com.typesafe.play" %% "play-json"    % "2.2.1"

libraryDependencies += "com.amazonaws"       % "aws-java-sdk" % "1.6.9.1"

libraryDependencies += "org.specs2"         %% "specs2"       % "2.3.10"   % "test"