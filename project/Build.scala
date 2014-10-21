import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "DetektifLingkungan"
    val appVersion      = "1.0"

    val appDependencies = Seq(
//        
//      "org.scala-tools" %% "scala-stm" % "0.3",
//      "org.apache.derby" % "derby" % "10.4.1.3" % "test"
      "mysql" % "mysql-connector-java" % "5.1.18",
      "com.google.code.gson" % "gson" % "2.2.4"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(defaultScalaSettings: _*).settings(
      
//      resolvers += "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/",
//      resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"
            
    )

}
