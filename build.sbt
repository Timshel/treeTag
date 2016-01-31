name := "treeTag"

version := "1.0-SNAPSHOT"
scalaVersion := "2.11.6"

scalacOptions ++= Seq(
  "-Yrangepos",
  "-Xlint",
  "-deprecation",
  "-Xfatal-warnings",
  "-feature",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen"
// "-Ywarn-unused-import"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  cache,
  jdbc,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.5.0",
  "io.github.jto" %% "validation-core" % "1.1",
  "io.github.jto" %% "validation-json" % "1.1",
  "com.chuusai" %% "shapeless" % "2.2.5",
  "org.scalaz" %% "scalaz-core" % "7.2.0"
)

routesImport  ++= Seq(
  "controllers.RouteBindings._",
  "models._"
)

routesGenerator := InjectedRoutesGenerator