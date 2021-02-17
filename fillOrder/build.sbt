lazy val app = project in file(".") enablePlugins(Cinnamon)

cinnamonSuppressRepoWarnings := true

cinnamon in run := true
cinnamon in test := true

cinnamonLogLevel := "INFO"

name := "test"

version := "0.1"

scalaVersion := "2.13.4"

val AkkaVersion = "2.6.5"
val AkkaHttpVersion = "10.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  Cinnamon.library.cinnamonCHMetrics,
  Cinnamon.library.cinnamonAkkaTyped,
  Cinnamon.library.cinnamonPrometheus,
  Cinnamon.library.cinnamonPrometheusHttpServer,
  Cinnamon.library.cinnamonAkka,
  Cinnamon.library.cinnamonAkkaPersistence,
  Cinnamon.library.cinnamonAkkaStream,
  Cinnamon.library.cinnamonAkkaProjection,
  Cinnamon.library.cinnamonAkkaHttp,
  Cinnamon.library.cinnamonSlf4jEvents,
  Cinnamon.library.cinnamonJvmMetricsProducer,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)