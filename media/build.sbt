import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val akkaVersion = "2.3.4"

//lazy val depProject = RootProject(uri("git://github.com/camfire/webrtc-jackson-serialization#master"))

val project = Project(
  id = "akka-cluster-sharding-scala",
  base = file("."),
  settings = Project.defaultSettings ++ SbtMultiJvm.multiJvmSettings ++ Seq(
    name := "akka-cluster-sharding-scala",
    version := "1.0",
    scalaVersion := "2.11.2",
    resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven",
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-jackson" % "3.2.10",
      "com.github.romix.akka" %% "akka-kryo-serialization" % "0.3.2",
      "com.softwaremill.macwire" %% "macros" % "0.7",
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "com.etaty.rediscala" %% "rediscala" % "1.4.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % "2.1.6" % "test",
      "commons-io" % "commons-io" % "2.4" % "test"),
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target, 
    // and combine the results from ordinary test and multi-jvm tests
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults)  =>
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
          Tests.Output(overall,
            testResults.events ++ multiNodeResults.events,
            testResults.summaries ++ multiNodeResults.summaries)
    }
  )
) configs (MultiJvm)

//dependsOn(depProject)

