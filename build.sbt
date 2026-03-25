ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "metrics",
  )


organization := "io.github.ivagafonov"
organizationName := "none"
organizationHomepage := None

scmInfo := Some(
  ScmInfo(
    url("https://github.com/IVAgafonov/metrics-support"),
    "scm:git@github.com/IVAgafonov/metrics-support.git"
  )
)
developers := List(
  Developer(
    id = "ivagafonov",
    name = "Igor Agafonov",
    email = "igoradm90@gmail.com",
    url = url("https://github.com/IVAgafonov")
  )
)

description := "Scala Prometheus metrics support"
licenses := List(License.Apache2)
homepage := Some(url("https://github.com/IVAgafonov/metrics-support"))


pomIncludeRepository := { _ => false }
publishMavenStyle := true

publishTo :=  Some("central-snapshots" at "https://central.sonatype.com/repository/maven-snapshots/")

resolvers += "Akka library repository" at "https://repo.akka.io/VwWuWqHio4Q6iq_HwEbwTFVEeDwyIqCSWT00vOG2a305Uev1/secure"

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
