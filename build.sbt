val scala213Version = "2.13.8"

val catsVersion         = "2.7.0"
val catsEffectVersion   = "3.3.7"
val http4sVersion       = "1.0.0-M31"
val fs2Version          = "3.2.5"
val circeVersion        = "0.14.1"
val log4catsVersion     = "2.2.0"
val logbackVersion      = "1.2.11"
val scalaTestVersion    = "3.2.11"
val catsEffectScalaTest = "1.4.0"

lazy val root = project
  .in(file("."))
  .settings(
    name         := "podman-scala-client",
    organization := "io.podmanclient",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala213Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"                     % catsVersion,
      "org.typelevel" %% "cats-effect"                   % catsEffectVersion,
      "org.http4s"    %% "http4s-dsl"                    % http4sVersion,
      "org.http4s"    %% "http4s-client"                 % http4sVersion,
      "org.http4s"    %% "http4s-ember-core"             % http4sVersion,
      "org.http4s"    %% "http4s-ember-client"           % http4sVersion,
      "org.http4s"    %% "http4s-circe"                  % http4sVersion,
      "co.fs2"        %% "fs2-core"                      % fs2Version,
      "co.fs2"        %% "fs2-io"                        % fs2Version,
      "io.circe"      %% "circe-core"                    % circeVersion,
      "io.circe"      %% "circe-generic"                 % circeVersion,
      "io.circe"      %% "circe-parser"                  % circeVersion,
      "org.typelevel" %% "log4cats-slf4j"                % log4catsVersion,
      "ch.qos.logback" % "logback-classic"               % logbackVersion,
      "org.scalatest" %% "scalatest"                     % scalaTestVersion    % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectScalaTest % Test,
    ),
  )
