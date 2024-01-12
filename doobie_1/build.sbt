name := "doobie-recipes"

version := "1.0.0"

scalaVersion := "2.13.12"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:postfixOps",
  "-feature",
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-explaintypes", // Explain type errors in more detail.
  "-Wvalue-discard",
  "-Ywarn-unused"
)

val doobieVersion = "1.0.0-RC1"

libraryDependencies ++= Seq(
  "org.tpolecat"       %% "doobie-core"     % doobieVersion,
  "org.tpolecat"       %% "doobie-postgres" % doobieVersion,
  "org.tpolecat"       %% "doobie-hikari"   % doobieVersion,
  "com.github.gekomad" %% "itto-csv"        % "1.2.0",
  "org.scalatest"      %% "scalatest"       % "3.3.0-SNAP3" % Test
)

Test / parallelExecution := false
