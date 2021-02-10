name := "2021_Trading_System"

version := "0.1"

scalaVersion := "2.12.13"

libraryDependencies ++= {
  val akkaVersion = "2.5.32"
  val akkaHttpVersion = "10.0.5"
  val camelVersion    = "2.19.0"
  val activeMQVersion = "5.7.0"
  Seq(
    "org.scala-lang.modules"  %% "scala-xml"    	                 % "1.0.6",
    "com.typesafe.akka"       %% "akka-camel"                        % akkaVersion,
    "net.liftweb"             %% "lift-json"                         % "3.0.1",
    "com.typesafe.akka"       %% "akka-actor"                        % akkaVersion,
    "com.typesafe.akka"       %% "akka-slf4j"                        % akkaVersion,
    "com.typesafe.akka"       %% "akka-stream"                       % akkaVersion,
    "com.typesafe.akka"       %% "akka-http-core"                    % akkaHttpVersion,
    "com.typesafe.akka"       %% "akka-http"                         % akkaHttpVersion,
    "com.typesafe.akka"       %% "akka-http-spray-json"              % akkaHttpVersion,
    "com.typesafe.akka"       %% "akka-http-xml"                     % akkaHttpVersion,
    "com.typesafe.akka"       %% "akka-http-testkit"                 % akkaHttpVersion     % "test",
    "ch.qos.logback"          %  "logback-classic"                   % "1.2.3",
    "commons-io"              %  "commons-io"                        % "2.5"         % "test",
    "org.apache.camel"        %  "camel-mina"                        % camelVersion 	 % "test",
    "org.apache.camel"        %  "camel-jms"                         % camelVersion 	 % "test",
    "org.apache.activemq"     %  "activemq-camel"                    % activeMQVersion % "test",
    "org.apache.activemq"     %  "activemq-core"                     % activeMQVersion % "test",
    "org.apache.camel"        %  "camel-jetty"                       % camelVersion 	 % "test",
    "com.typesafe.akka"       %% "akka-testkit"                      % akkaVersion     % "test",
    "org.scalatest"           %% "scalatest"                         % "3.0.1"         % "test"

  )
}
