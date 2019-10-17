name := "doobie-recipes"

version := "0.5.0"

scalaVersion := "2.13.1"

scalacOptions += "-deprecation"

lazy val doobieVersion = "0.8.4"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
)

// test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0-M1" % Test

libraryDependencies += "com.github.gekomad" %% "itto-csv" % "1.0.0" % Test

parallelExecution in Test := false
