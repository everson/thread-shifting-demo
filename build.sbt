name := "thread-shifting"
version := "0.1"
scalaVersion := "2.13.10"

libraryDependencies ++= Seq(
  "io.netty" % "netty-all" % "4.1.75.Final",
  "io.vertx" % "vertx-web-client" % "4.3.4",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "org.scalaz" %% "scalaz-core" % "7.2.30",
  "org.scalaz" %% "scalaz-concurrent" % "7.2.30",
  "org.typelevel" %% "cats-effect" % "3.5.7",
)
