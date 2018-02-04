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
  "-Ywarn-numeric-widen"
// "-Ywarn-unused-import"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  ehcache,
  jdbc,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "io.github.jto" %% "validation-core" % "2.1.1",
  "io.github.jto" %% "validation-playjson" % "2.1.1",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.scalaz" %% "scalaz-core" % "7.2.19"
)

routesImport  ++= Seq(
  "controllers.RouteBindings._",
  "models._"
)

routesGenerator := InjectedRoutesGenerator