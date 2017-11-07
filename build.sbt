name := "YACCoin"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.+",
  "com.typesafe.akka" %% "akka-remote" % "2.5.+",
  "org.scorexfoundation" %% "scrypto" % "2.0.+"
)
        