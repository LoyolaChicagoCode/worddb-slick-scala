version := "0.0.2"

name := "worddb-slick-scala"

scalaVersion := "2.13.6"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint:_")

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "mainargs" % "0.2.1",
  "com.lihaoyi" %% "os-lib" % "0.7.8",
  "org.typelevel" %% "cats-core" % "2.6.1",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.xerial" % "sqlite-jdbc" % "3.36.0.1",
  "org.log4s" %% "log4s" % "1.10.0",
  "org.slf4j" % "slf4j-simple" % "1.7.32",
  "org.scalameta" %% "munit" % "0.7.28" % Test
)

Test / parallelExecution := false

enablePlugins(JavaAppPackaging)

maintainer := "laufer@cs.luc.edu"
