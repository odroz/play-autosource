import play.PlayScala
import sbt._
import Keys._
import bintray.Plugin.bintraySettings
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {
  val buildName    = "play-autosource"

  // coreVersion is the version of Autosource specification
  // each implementation of the spec should have the same major version
  // but can evolve with its own minor version
  // For example:
  // coreVersion = 2.0 -> reactiveMongo v2.0, 2.1, 2.2, 2.3...
  val coreVersion  = "2.1"

  val mandubianRepo = Seq(
    "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
    "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/",
    "Mandubian bintray repository releases" at "http://dl.bintray.com/mandubian/maven",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  object V {
    val play = "2.3.3"
  }

  val BuildSettings = Defaults.defaultSettings ++ Seq(
    scalaVersion := "2.11.0",
    crossScalaVersions := Seq("2.10.4", "2.11.0"),
    organization := "play-autosource",

    resolvers ++= mandubianRepo
  ) ++ Publish.settings

  lazy val main = Project(
    id = buildName,
    base = file("."),
    settings = BuildSettings ++ Seq(
      publish      := {}
    )
  ) aggregate(
    core,
    reactivemongo,
    datomisca,
    slick
    /*couchbase*/
  )

  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = BuildSettings ++ Seq(
      version := coreVersion,

      libraryDependencies ++= Seq(
        "com.mandubian"     %% "play-json-zipper"  % "1.2"                      ,
        "com.typesafe.play" %% "play-json"         % V.play                     ,
        "com.typesafe.play" %% "play"              % V.play         % "provided",
        "org.specs2"        %% "specs2"            % "2.3.12"         % "test"  ,
        "junit"              % "junit"             % "4.8"          % "test"
      )
    )
  )

  lazy val reactivemongo = Project(
    id = "reactivemongo",
    base = file("reactivemongo"),
    settings = BuildSettings ++ Seq(
      // can be customized by keeping major version of the core version
      version := "2.1-SNAPSHOT",

      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play"                % V.play         % "provided",
        "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"
      )
    )
  ) dependsOn(core)

  reactivemongo.enablePlugins(PlayScala)

  lazy val datomisca = Project(
    id = "datomisca",
    base = file("datomisca"),
    settings = BuildSettings ++ Seq(
      // can be customized by keeping major version of the core version
      version := "2.1",

      resolvers ++= Seq(
        "datomisca-mvn-repo" at "http://dl.bintray.com/content/pellucid/maven",
        "clojars"   at "https://clojars.org/repo"
      ),
      libraryDependencies ++= Seq(
        "com.pellucid"      %% "datomisca"      % "0.7-alpha-11",
        "com.datomic"        % "datomic-free"   % "0.9.4766.16" % "provided" exclude("org.slf4j", "slf4j-nop"),
        "com.typesafe.play" %% "play"           % V.play     % "provided"
      )
    )
  ) dependsOn(core)
/*
  lazy val couchbase = Project(
    id = "couchbase",
    base = file("couchbase"),
    settings = BuildSettings ++ Seq(
      // can be customized by keeping major version of the core version
      version := "2.1-SNAPSHOT",

      resolvers ++= Seq(
        "ReactiveCouchbase Snapshots" at "https://raw.github.com/ReactiveCouchbase/repository/master/snapshots/"
      ),
      libraryDependencies ++= Seq(
        "org.reactivecouchbase" %% "reactivecouchbase-core" % "0.3-SNAPSHOT",
        "org.reactivecouchbase" %% "reactivecouchbase-play" % "0.3-SNAPSHOT",
        "com.typesafe.play"     %% "play"                   % V.play          % "provided",
        "com.typesafe.play"     %% "play-cache"             % V.play          % "provided"
      )
    )
  ) dependsOn(core)
*/

  lazy val slick = Project(
    id = "slick",
    base = file("slick"),
    settings = BuildSettings ++ Seq(
      // can be customized by keeping major version of the core version
      version := "2.1-SNAPSHOT",

      resolvers ++= Seq(
        "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
        "Typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
        "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
      ),

      libraryDependencies ++= Seq(
        "com.typesafe.play"   %%  "play-slick"    % "0.8.0",
        "com.typesafe.play"   %%  "play"          % V.play         % "provided",
        "com.typesafe.play"   %%  "play-jdbc"     % V.play         % "provided"
      )
    )
  ) dependsOn(core)

  object Publish {
    lazy val settings = Seq(
      publishMavenStyle := true,
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      publishTo <<= version { (version: String) =>
        val localPublishRepo = "../mandubian-mvn/"
        if(version.trim.endsWith("SNAPSHOT"))
          Some(Resolver.file("snapshots", new File(localPublishRepo + "/snapshots")))
        else Some(Resolver.file("releases", new File(localPublishRepo + "/releases")))
      }
    ) ++ bintraySettings
  }
}
