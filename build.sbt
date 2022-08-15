lazy val treeTag = project
  .in(file("."))
  .enablePlugins(PlayScala)
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
      ehcache,
      jdbc,
      evolutions,
      "org.tpolecat" %% "doobie-core" % "0.13.4",
      "org.tpolecat" %% "doobie-postgres"  % "0.13.4", 
      "org.tpolecat" %% "doobie-scalatest" % "0.13.4" % Test,
      "com.typesafe.play" %% "play-json" % "2.8.2",
      "com.chuusai" %% "shapeless" % "2.3.7"
    ),
    routesImport  ++= Seq(
      "controllers.RouteBindings._",
      "models._"
    ),
    routesGenerator := InjectedRoutesGenerator
  )
