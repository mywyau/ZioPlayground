ThisBuild / scalaVersion := "2.13.6"

ThisBuild / organization := "mikey.zio.example"

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val hello = (project in file("."))
  .settings(
    name := "Hello",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % Test,
    libraryDependencies += "dev.zio" %% "zio" % "1.0.12",
    libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.12"
  )