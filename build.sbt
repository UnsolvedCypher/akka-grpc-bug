
name := "akka-grpc-bug"

version := "0.1"

scalaVersion := "2.13.7"

enablePlugins(AkkaGrpcPlugin)
libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.1.2"
