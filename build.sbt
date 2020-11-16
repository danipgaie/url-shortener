name := "url-shortener"
 
version := "1.0" 
      
lazy val `url-shortener` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

resolvers += ("dl-john-ky-releases" at "http://dl.john-ky.io/maven/releases").withAllowInsecureProtocol(true)

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice,
  "org.picoworks" %% "pico-hashids"  % "4.5.151",
  "net.debasishg" %% "redisclient" % "3.30",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test",
  "org.mockito" %% "mockito-scala" % "1.15.0" % "test"
)

val circeVersion = "0.12.3"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

unmanagedResourceDirectories in Test += baseDirectory ( _ /"target/web/public/test" ).value

