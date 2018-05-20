scalacOptions += "-Ypartial-unification"
resolvers += Opts.resolver.sonatypeSnapshots
libraryDependencies ++= Seq(
  "com.lightbend" %% "kafka-streams-scala" % "0.2.1",
  "org.typelevel" %% "cats-core" % "1.0.1",
  "com.goyeau" %% "kafka-streams-circe" % "0.5",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
) ++ circe ++ monocle

lazy val circe = {
  val version = "0.9.3"
  Seq(
    "io.circe" %% "circe-core" % version,
    "io.circe" %% "circe-parser" % version,
    "io.circe" %% "circe-java8" % version,
    "io.circe" %% "circe-generic" % version
  )
}

lazy val monocle = {
  val monocleVersion = "1.5.0-cats"
  Seq(
    "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
    "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion
  )
}
