version := "0.0.1"

name := "slick-explorations-scala"

scalaVersion := "2.13.5"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint:_")

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "org.log4s" %% "log4s" % "1.9.0",
  "org.slf4j" % "slf4j-simple" % "1.7.30",
  "com.h2database" % "h2" % "1.4.200"
)
