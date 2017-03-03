import sbt._

object Dependencies {

	// Versions
	val awsClientVersion = "1.11.98"

	// Dependencies
	lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"
	lazy val awsStepFunctions = "com.amazonaws" % "aws-java-sdk-stepfunctions" % awsClientVersion
	lazy val playJson = "com.typesafe.play" %% "play-json" % "2.5.12"

}
