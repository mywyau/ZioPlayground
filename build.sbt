ThisBuild / scalaVersion := "2.13.6"

ThisBuild / organization := "mikey.zio.example"

lazy val hello = (project in file("."))
  .settings(
    name := "Hello",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % Test
  )