# The Difficulty

1. There are systems that do not implement [Unicode equivalence](https://en.wikipedia.org/wiki/Unicode_equivalence),
	which may cause interoperability issues that are hard to fix.
	1. They are hard since two different but canonically equivalent sequences of characters are indistinguishable on the screen.

2. [XFS](https://en.wikipedia.org/wiki/XFS), the default file system in RHEL 7, is one of such systems.
	1. The following code will throw `NoSuchFileException` on XFS, but completes successfully on macOS/Windows:

		```scala
		import java.nio.file.{Paths, Files}

		object FileNameGa extends App {
			val name1 = "が.txt" // precomposed character ('\u304c')
			val name2 = "が.txt" // decomposed character ('\u304b', '\u3099') that looks identical

			// create a file with name1, and read the file with name2
			Files.write(Paths.get(name1), Array[Byte](0))
			Files.readAllBytes(Paths.get(name2)) // the file is found only if the system implements Unicode equivalence
		}
		```


# A Remedy

1. The problem can be largely avoided by not mixing [precomposed](https://en.wikipedia.org/wiki/Precomposed_character) / decomposed characters.

1. Unicode [defines](http://unicode.org/reports/tr15/),
	and Java [supports](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/Normalizer.html)
	the normalization forms that can solve the problem, but every one of them brings new obstacles.
	1. NFD (decomposed) is not easy to work with; the vast majority of programming/authoring tools produces precomposed characters.
		1. One popular source of NFD strings is the file name in macOS Finder.
			If a file name is copy-pasted to Vim, there most likely is a problem
			since the macOS input method produces precomposed characters.

	1. NFC (precomposed) not only composes characters, but also consolidates similar characters
		into [CJK Unified Ideographs](https://en.wikipedia.org/wiki/CJK_Unified_Ideographs),
		while the vast majority of people casually distinguish those similar characters that carry subtly different sentiments.


## Scala compiler plugin

The plugin warns/compose decomposed Unicode characters.


# Build Environment

1. Java 1.8.0_201

2. [mill](http://www.lihaoyi.com/mill/) 0.3.6
	1. On macOS, [Homebrew](https://brew.sh/) can be used for installation

			brew install mill


## Commands

1. Build and publish to `~/.ivy2/local`

		mill plugin.publishLocal

2. Publish to Maven Central

		mill plugin.publish --sonatypeCreds "user:pass" --release true

	1. macOS: install `gnupg` with `brew`
	2. mill-0.3.6: `mill plugin.publish` fails with `os.SubprocessException: CommandResult 2` if `gpg` tries to ask for the passphrase
		1. To avoid the failure, make `gpg-agent` provide the passphrase, e.g., `LANG=en_US.UTF8 gpg -ab README.md`


# References

1. https://docs.scala-lang.org/overviews/plugins/index.html
2. https://typelevel.org/scala/docs/phases.html
3. https://tama-san.com/unicode-nfc/
