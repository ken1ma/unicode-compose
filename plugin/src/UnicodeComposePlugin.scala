package jp.ken1ma.scalac.plugin.unicode

import scala.annotation.tailrec
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import com.ibm.icu.text.Normalizer2

class UnicodeComposePlugin(val global: Global) extends Plugin { plugin =>
	import global._

	val name = "unicode-compose"
	val description = "Warns of/composes decomposed Unicode characters"
	val components = List[PluginComponent](Component)

	/*
		TODO: suppport the following options

		form
			precomposed (default)
			decomposed
			nfc
			nfd

		unnormalized
			ignore
			warn
			error (default)

		halfwidthKana
			ignore
			warn (default)
			error
			toFullwidth
	*/

	private object Component extends PluginComponent {
		val global: plugin.global.type = plugin.global
		val phaseName = plugin.name
		val runsAfter = List("parser")
		def newPhase(prev: Phase) = new StdPhase(prev) {
			def apply(unit: CompilationUnit) {
				for (tree @ Literal(Constant(value: String)) <- unit.body)
					warnDecomposed(value, tree)
			}
		}
	}

	val nfc = Normalizer2.getNFCInstance

	def warnDecomposed(string: String, tree: Literal) {
		@tailrec def loop(index: Int) {
			if (index + 1 < string.size) {
				if (string(index).isHighSurrogate) {
					require(string(index + 1).isLowSurrogate, s"high surrogate is not followed by low surrogate: ${renderChars(string.slice(index, index + 2))}")
					if (index + 2 < string.size) {
						val codePoint0 = java.lang.Character.toCodePoint(string(index), string(index + 1))

						// 2 surrogates
						if (string(index + 2).isHighSurrogate) {
							require(index + 3 < string.size, s"high surrogate at the end of input: ${renderChar(string(index + 2))}")
							require(string(index + 3).isLowSurrogate, s"high surrogate is not followed by low surrogate: ${renderChars(string.slice(index + 2, index + 4))}")
							val codePoint1 = java.lang.Character.toCodePoint(string(index + 2), string(index + 3))
							if (nfc.composePair(codePoint0, codePoint1) >= 0)
								global.reporter.warning(tree.pos, s"decomposed Unicode character: ${string.slice(index, index + 4)} (${renderChars(string.slice(index, index + 4))})")
							else
								loop(index + 2)

						// 1 surrogate
						} else {
							if (nfc.composePair(codePoint0, string(index + 2)) >= 0)
								global.reporter.warning(tree.pos, s"decomposed Unicode character: ${string.slice(index, index + 3)} (${renderChars(string.slice(index, index + 3))})")
							else
								loop(index + 2)
						}
					}

				// not surrogate
				} else {
					if (nfc.composePair(string(index), string(index + 1)) >= 0)
						global.reporter.warning(tree.pos, s"decomposed Unicode character: ${string.slice(index, index + 2)} (${renderChars(string.slice(index, index + 2))})")
					else
						loop(index + 1)
				}
			}
		}
		loop(0)

		// http://www.unicode.org/charts/PDF/UFF00.pdf
/*
		string.indexWhere(ch => ch >= '\uff61' && ch <= '\uff9f') match {
			case -1 =>
			case index =>
				global.reporter.warning(tree.pos, f"halfwidth kana: ${string(index)} ('\\u${string(index)}%04x')")
		}
*/
	}

	def renderChar(ch: Char) = f"'\\u$ch%04x'"
	def renderChars(seq: Seq[Char]) = seq.map(renderChar).mkString(", ")
}
