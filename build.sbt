name := "disconsul"

version := "1.0"

scalaVersion := "2.11.8"

organization := "com.ilunin"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % Test
)

coverageMinimum := 95

coverageFailOnMinimum := true

crossScalaVersions := Seq("2.10.6", "2.11.8")