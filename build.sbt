name := "treeTag"

version := "1.0-SNAPSHOT"
scalaVersion := "2.12.4"

scalacOptions ++= Seq(
  "-Yrangepos",
  "-Xlint",
  "-deprecation",
  "-feature",
  "-encoding", "UTF-8",
//  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ypartial-unification"
// "-Ywarn-unused-import"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  ehcache,
  jdbc,
  evolutions,
  "org.tpolecat" %% "doobie-core" % "0.5.1",
  "org.tpolecat" %% "doobie-postgres"  % "0.5.1", 
  "org.tpolecat" %% "doobie-scalatest" % "0.5.1" % Test,
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "io.github.jto" %% "validation-core" % "3.0.0-SNAPSHOT",
  "io.github.jto" %% "validation-playjson" % "3.0.0-SNAPSHOT",
  "com.chuusai" %% "shapeless" % "2.3.3"
)

routesImport  ++= Seq(
  "controllers.RouteBindings._",
  "models._"
)

routesGenerator := InjectedRoutesGenerator