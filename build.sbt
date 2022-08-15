val Http4sVersion = "0.23.11"
val CirceVersion = "0.14.2"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.11"
val MunitCatsEffectVersion = "1.0.7"

lazy val treeTag = project.in(file("."))
  .settings(
    name := "treeTag",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.8",
    semanticdbEnabled := true,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-encoding", "UTF-8",
      "-language:higherKinds"
    ),
    javaOptions += "-Duser.timezone=UTC",
    resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    libraryDependencies ++= Seq(
      "org.http4s"   %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"   %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"   %% "http4s-dsl"          % Http4sVersion,
      "org.http4s"   %% "http4s-play-json"    % Http4sVersion,
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-postgres"  % "1.0.0-RC2", 
      "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC2" % Test,
      "com.typesafe.play" %% "play-json" % "2.9.2",
      "com.chuusai" %% "shapeless" % "2.3.7",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion         % Runtime,
      "org.scalameta"   %% "munit"               % MunitVersion           % Test,
      "org.typelevel"   %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
