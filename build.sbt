name := "learning-spray"

scalaVersion := "2.10.2"

version := "1.0"

resolvers ++= Seq(
	"spray repo" at "http://repo.spray.io",
	"Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
	"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
	)

libraryDependencies ++= Seq(
  "io.spray" % "spray-can" % "1.1-M8",
  "io.spray" % "spray-routing" % "1.1-M8",
  "io.spray" % "spray-testkit" % "1.1-M8",
  "com.typesafe.akka" %% "akka-actor" % "2.1.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.1.4",
	"com.typesafe.akka" % "akka-slf4j" % "2.0.3", 
    "ch.qos.logback" % "logback-classic" % "1.0.9"
	)
