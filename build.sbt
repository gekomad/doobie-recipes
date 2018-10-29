name := "doobie-exercises"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions += "-Ypartial-unification"

lazy val doobieVersion = "0.6.0"
lazy val scalatestVersion = "3.0.5"
lazy val cormorantVersion = "0.0.7"

libraryDependencies ++= Seq(
  //doobie
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,

  //cormorant csv
  "io.chrisdavenport" %% "cormorant-core" % cormorantVersion,
  "io.chrisdavenport" %% "cormorant-generic" % cormorantVersion,

  // test
  "org.scalatest" %% "scalatest" % scalatestVersion % "test"
)


parallelExecution in Test := false