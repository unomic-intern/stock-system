name := "test"

version := "0.1"

scalaVersion := "2.13.4"

val AkkaVersion = "2.6.5"
val AkkaHttpVersion = "10.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka"       %% "akka-actor"                                 % AkkaVersion,
  "com.typesafe.akka"       %% "akka-actor-typed"                           % AkkaVersion,
  "com.typesafe.akka"       %% "akka-slf4j"                                 % AkkaVersion,
  "com.typesafe.akka"       %% "akka-stream"                                % AkkaVersion,
  "com.typesafe.akka"       %% "akka-http"                                  % AkkaHttpVersion,
  "com.typesafe.akka"       %% "akka-http-spray-json"                       % AkkaHttpVersion,
  "ch.qos.logback"          % "logback-classic"                             % "1.2.3",
  "com.typesafe.akka"       %% "akka-testkit"                               % AkkaVersion        %   "test",
  "org.scalatest"           %% "scalatest"                                  % "3.1.4"            %   Test,
  "com.typesafe.akka"       %% "akka-actor-testkit-typed"                   % AkkaVersion        %   Test,
  "com.typesafe.akka"       %% "akka-http-testkit"                          % AkkaHttpVersion    % "test",
  "com.typesafe.akka"       %% "akka-stream-testkit"                        % AkkaVersion

)
