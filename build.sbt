name := """PdfColorCounter"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.apache.pdfbox" % "pdfbox" % "1.8.6",
  "bouncycastle" % "bcprov-jdk15" % "140",
  "mysql" % "mysql-connector-java" % "5.1.18"
)

mappings in Universal ++=
  (baseDirectory.value / "samples" * "*" get) map
    (x => x -> ("samples/" + x.getName))

