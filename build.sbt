

name := "play-dynamodb"

version := "0.1"

organization := "com.mediamath.play"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-encoding", "utf-8", "-deprecation", "-unchecked", "-feature")

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies +=  "com.typesafe.play" %% "play-json" % "2.2.0-RC1"

libraryDependencies += "com.amazonaws"      % "aws-java-sdk"       % "1.5.4"
