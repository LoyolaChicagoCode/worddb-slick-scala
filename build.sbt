version := "0.0.2"

name := "worddb-slick-scala"

scalaVersion := "2.13.8"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint:_")

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "mainargs" % "0.2.3",
  "com.lihaoyi" %% "os-lib" % "0.8.1",
  "org.typelevel" %% "cats-core" % "2.10.0",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.xerial" % "sqlite-jdbc" % "3.39.2.0",
  "org.log4s" %% "log4s" % "1.10.0",
  "org.slf4j" % "slf4j-simple" % "1.7.36",
  "org.scalameta" %% "munit" % "0.7.29" % Test
)

Test / parallelExecution := false

enablePlugins(JavaAppPackaging)

maintainer := "laufer@cs.luc.edu"
