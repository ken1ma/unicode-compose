import java.nio.file.{Paths, Files}

object FileNameGa extends App {
	val name1 = "が.txt" // precomposed character
	val name2 = "が.txt" // decomposed character that looks identical

	def dumpBaseName(s: String) = (0 until s.codePointCount(0, s.indexOf('.'))).map(s.codePointAt).map(cp => f"$cp%04x").mkString(" ")
	println(s"name1: ${dumpBaseName(name1)}") // prints 304c
	println(s"name2: ${dumpBaseName(name2)}") // prints 304b 3099

	// create a file with name1, and read the file with name2
	Files.write(Paths.get(name1), Array[Byte](0))
	Files.readAllBytes(Paths.get(name2)) // the file is found only if the system implements Unicode equivalence
}
