import _root_.scoverage.ScoverageSbtPlugin

name := "disconsul"

version := "1.0"

scalaVersion := "2.11.8"

organization := "com.ilunin"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % Test
)

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 95

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true