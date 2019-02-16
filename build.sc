import mill._, scalalib._, publish._

trait CommonScalaModule extends ScalaModule {
	def scalaVersion = "2.12.8"
	def scalacOptions = Seq(
		// the same as https://scastie.scala-lang.org
		"-encoding", "UTF-8",
		"-deprecation",
		"-feature",
		"-unchecked",
	)
}

object plugin extends CommonScalaModule with PublishModule {
	def artifactName = "unicode-compose-plugin"
	def publishVersion = "0.0.2"
	def pomSettings = PomSettings(
		description = "A Scala compiler plugin that warns of/composes decomposed Unicode characters",
		organization = "jp.ken1ma",
		url = "https://github.com/ken1ma/unicode-compose",
		Seq(License.MIT),
		VersionControl.github("ken1ma", "unicode-compose"),
		Seq(
			Developer("ken1ma", "Kenichi Masuko","https://github.com/ken1ma")
		)
	)

	def ivyDeps = Agg(
		ivy"com.ibm.icu:icu4j:63.1",
		ivy"${scalaOrganization()}:scala-compiler:${scalaVersion()}",
	)
}

object examples extends CommonScalaModule
