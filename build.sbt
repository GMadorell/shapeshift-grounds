scalaVersion := "2.12.4"

version := "1.0"

scalacOptions += "-Ypartial-unification"
scalacOptions += "-language:higherKinds"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.0.0-RC1"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.scalamock" %% "scalamock" % "4.0.0" % Test
)
