name := "activator-akka-cassandra"

version := "1.0"

scalaVersion := "2.10.3"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-actor"            % "2.2.3",
  "com.typesafe.akka"      %% "akka-slf4j"            % "2.2.3",
  "io.spray"                % "spray-can"             % "1.2-RC4",
  "io.spray"                % "spray-client"          % "1.2-RC4",
  "io.spray"                % "spray-routing"         % "1.2-RC4",
  "io.spray"               %% "spray-json"            % "1.2.3",
  "org.specs2"             %% "specs2"                % "2.3.4"        % "test",
  "io.spray"                % "spray-testkit"         % "1.2-RC4"      % "test",
  "com.typesafe.akka"      %% "akka-testkit"          % "2.2.3"        % "test"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

parallelExecution in Test := false