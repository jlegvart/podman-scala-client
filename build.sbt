val scala3Version = "3.1.1"

name := "scala-shopping-cart"

version := "1.0"

val catsVersion       = "2.70"
val catsEffectVersion = "3.3.7"
val http4sVersion     = "1.0.0-M31"
val circeVersion      = "0.15.0-M1"
val pureConfigVersion = "0.17.1"
val log4catsVersion   = "2.2.0"
val logbackVersion    = "1.2.11"

lazy val root = project
  .in(file("."))
  .settings(
    name         := "podman-scala-client",
    organization := "io.podmanclient",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-core"           % catsVersion,
      "org.typelevel"         %% "cats-effect"         % catsEffectVersion,
      "org.http4s"            %% "http4s-dsl"          % http4sVersion,
      "org.http4s"            %% "http4s-client"       % http4sVersion,
      "org.http4s"            %% "http4s-ember-core"   % http4sVersion,
      "org.http4s"            %% "http4s-ember-client" % http4sVersion,
      "org.http4s"            %% "http4s-circe"        % http4sVersion,
      "io.circe"              %% "circe-generic"       % circeVersion,
      "io.circe"              %% "circe-generic"       % circeVersion,
      "io.circe"              %% "circe-literal"       % circeVersion,
      "com.github.pureconfig" %% "pureconfig"          % pureConfigVersion,
      "org.typelevel"         %% "log4cats-slf4j"      % log4catsVersion,
      "ch.qos.logback"         % "logback-classic"     % logbackVersion,
    ),
  )