version := "0.0.1"

name := "worddb-slick-scala"

scalaVersion := "2.13.6"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint:_")

libraryDependencies ++= Seq(
  "com.github.alexarchambault" %% "case-app" % "2.0.6",
  "com.github.alexarchambault" %% "case-app-cats" % "2.0.6",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.xerial" % "sqlite-jdbc" % "3.34.0",
  "org.log4s" %% "log4s" % "1.9.0",
  "org.slf4j" % "slf4j-simple" % "1.7.30",
  "org.scalameta" %% "munit" % "0.7.25" % Test
)

Test / parallelExecution := false

enablePlugins(JavaAppPackaging)

maintainer := "laufer@cs.luc.edu"
