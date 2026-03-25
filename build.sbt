ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "metrics",
  )

ThisBuild / organization := "io.github.ivagafonov"
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / homepage := Some(url("https://github.com/IVAgafonov/metrics-support"))
ThisBuild / licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / developers := List(
  Developer(
    "Igor <igoradm90@gmail.com>",
    "Igor Agafonov",
    "igoradm90@gmail.com",
    url("https://github.com/IVAgafonov"))
)

val prometheusVersion = "1.4.3"
val AkkaVersion = "2.9.3"
val AkkaHttpVersion = "10.6.3"

libraryDependencies ++= Seq(
  "io.prometheus" % "prometheus-metrics-core" % prometheusVersion,
  "io.prometheus" % "prometheus-metrics-exposition-textformats" % prometheusVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "io.spray" %% "spray-json" % "1.3.6",

  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test
)
