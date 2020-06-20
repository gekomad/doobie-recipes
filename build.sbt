name := "doobie-recipes"

version := "0.6.2"

scalaVersion := "2.13.1"

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding", "UTF-8", // Specify character encoding used by source files.
  "-language:postfixOps",
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-explaintypes", // Explain type errors in more detail.
  "-Xfatal-warnings" // Fail the compilation if there are any warnings.
)

lazy val doobieVersion = "0.9.0"

libraryDependencies ++= Seq(
  "org.tpolecat"        %% "doobie-core"      % doobieVersion,
  "org.tpolecat"        %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat"        %% "doobie-hikari"    % doobieVersion,
  "com.github.gekomad"  %% "itto-csv"         % "1.1.1",
  "org.scalatest"       %% "scalatest"        % "3.3.0-SNAP2"      % Test
)

parallelExecution in Test := false
