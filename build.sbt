ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "3.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.6.19"
)

lazy val root = (project in file("."))
  .settings(
    name := "KafkaProdCon"
  )
