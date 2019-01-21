name := "doobie-recipes"

version := "0.4.1"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"

lazy val doobieVersion = "0.6.0"

libraryDependencies ++= Seq(
  //doobie
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
)

// test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

libraryDependencies += "com.github.gekomad" %% "itto-csv" % "0.1.0"

parallelExecution in Test := false
