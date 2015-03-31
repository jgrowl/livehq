//import com.typesafe.sbt.SbtStartScript
//enablePlugins(SbtNativePackager)
//import com.typesafe.sbt.SbtNativePackager._
//import NativePackagerKeys._

//import NativePackagerKeys._


val akkaVersion = "2.3.9"

//enablePlugins(JavaAppPackaging)
//enablePlugins(AkkaAppPackaging)


//val folderName =
//  if (System.getProperty("os.name").startsWith("Windows")) "windows" else "linux"
//val libPath = Seq("some/common/path", s"lib/native/$folderName").mkString(java.io.File.pathSeparator)
//javaOptions in run += s"-Djava.library.path=$libPath"

val project = Project(
  id = "media",
  base = file("."),
  settings = Project.defaultSettings ++ Seq(
//    settings = Project.defaultSettings ++ SbtMultiJvm.multiJvmSettings ++ Seq(
    name := "media",
    version := "1.0",
    scalaVersion := "2.11.6",
    resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven",
    resolvers += Resolver.sonatypeRepo("public"),
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
      "com.github.scopt" %% "scopt" % "3.3.0",
      "org.scalatest" %% "scalatest" % "2.1.6" % "test",
      "commons-io" % "commons-io" % "2.4" % "test"),
    scalacOptions in (Compile,doc) ++= Seq("-Ymacro-expand:none")



    //,
//    // make sure that MultiJvm test are compiled by the default test compilation
//    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
//    // disable parallel tests
//    parallelExecution in Test := false,
//    // make sure that MultiJvm tests are executed by the default test target,
//    // and combine the results from ordinary test and multi-jvm tests
//    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
//      case (testResults, multiNodeResults)  =>
//        val overall =
//          if (testResults.overall.id < multiNodeResults.overall.id)
//            multiNodeResults.overall
//          else
//            testResults.overall
//          Tests.Output(overall,
//            testResults.events ++ multiNodeResults.events,
//            testResults.summaries ++ multiNodeResults.summaries)
//    }
  )
) //configs (MultiJvm)
//.settings(SbtStartScript.startScriptForClassesSettings: _*)
