name         := "play-dynamodb"

organization := "com.mediamath"

scalaVersion := "2.10.4"


scalacOptions ++= Seq("-encoding", "utf-8", "-deprecation", "-unchecked", "-feature")


resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies += "com.typesafe.play" %% "play-json"    % "2.2.1"

libraryDependencies += "com.amazonaws"       % "aws-java-sdk" % "1.6.9.1"

libraryDependencies += "org.specs2"         %% "specs2"       % "2.3.10"   % "test"


releaseSettings


publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/MediaMath/play-dynamodb</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
        <comments>A business-friendly OSS license</comments>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:MediaMath/play-dynamodb.git</url>
      <connection>scm:git:git@github.com:MediaMath/play-dynamodb.git</connection>
    </scm>
    <developers>
      <developer>
        <id>themodernlife</id>
        <name>Ian Hummel</name>
        <url>http://themodernlife.com</url>
      </developer>
      <developer>
        <id>alekseyig</id>
        <name>Aleksey Zhukov</name>
        <url></url>
      </developer>
    </developers>)


