name := "treeTag"

version := "1.0-SNAPSHOT"
scalaVersion := "2.12.10"

scalacOptions ++= Seq(
  "-Yrangepos",
  "-Xlint",
  "-deprecation",
  "-feature",
  "-encoding", "UTF-8",
//  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ypartial-unification",
  "-language:higherKinds"
// "-Ywarn-unused-import"
)

javaOptions += "-Duser.timezone=UTC"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  ehcache,
  jdbc,
  evolutions,
  "org.tpolecat" %% "doobie-core" % "0.8.8",
  "org.tpolecat" %% "doobie-postgres"  % "0.8.8", 
  "org.tpolecat" %% "doobie-scalatest" % "0.8.8" % Test,
  "com.typesafe.play" %% "play-json" % "2.7.4",
  "com.chuusai" %% "shapeless" % "2.3.3"
)

routesImport  ++= Seq(
  "controllers.RouteBindings._",
  "models._"
)

routesGenerator := InjectedRoutesGenerator