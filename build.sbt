import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.gu",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "SimplesStepFunctions",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      awsStepFunctions,
      playJson
    )
  )
