version := "0.0.2"

name := "worddb-slick-scala"

scalaVersion := "2.13.14"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint:_")

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "mainargs" % "0.2.5",
  "com.lihaoyi" %% "os-lib" % "0.10.4",
  "org.typelevel" %% "cats-core" % "2.12.0",
  "com.typesafe.slick" %% "slick" % "3.5.1",
  "org.xerial" % "sqlite-jdbc" % "3.46.1.0",
  "org.log4s" %% "log4s" % "1.10.0",
  "org.slf4j" % "slf4j-simple" % "2.0.16",
  "org.scalameta" %% "munit" % "1.0.1" % Test
)

Test / parallelExecution := false

enablePlugins(JavaAppPackaging)

maintainer := "laufer@cs.luc.edu"
