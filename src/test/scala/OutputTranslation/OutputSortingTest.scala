package OutputTranslation

import ElementGenerator.ElementTranslateToAlphabet
import UtilityClasses.{CedictEntry, Grapheme, OutputEntry, StaticFileCharInfoWithLetterConway}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import staticFileGenerators.cedictMap.GenerateCedictMap

import scala.collection.mutable

class OutputSortingTest extends AnyFlatSpec with Matchers {

  // create a test that tests if there are any z characters in cedict or conway

  it should "find any cedict or junda characters with z codes" in {
    val conwaySansCedict: Set[OutputEntry] = OutputSorting.conwayOutSansCedict
    //val cedictMap: Map[String, CedictEntry] = GenerateCedictMap.cedictMap
    val cedictOut: Set[OutputEntry] = OutputSorting.cedictSetOut
    val conwayMap: Map[Grapheme, StaticFileCharInfoWithLetterConway] = ElementTranslateToAlphabet.completeTranslatedConwayMap
    val lettersExceptZ = lettersSansAscii()

    val otuputStrWithLies: StringBuilder = new StringBuilder
    var overlap: mutable.Set[OutputEntry] = mutable.Set[OutputEntry]()
    var overlapChars: mutable.Set[Char] = mutable.Set[Char]()
    for (entry <- conwaySansCedict) {
      for (eachCode <- entry.codes) {
        for (eachLetter <- eachCode) {
          if (!lettersExceptZ.contains(eachLetter) && eachCode.size > 1) {
            overlap.add(entry)
            overlapChars.add(eachLetter)
          }
        }
      }
    }
    for (entry <- cedictOut) {
      for (eachCode <- entry.codes) {
        for (eachLetter <- eachCode) {
          if (!lettersExceptZ.contains(eachLetter) && eachCode.size > 1) {
            val allGraphemes: Set[Grapheme] = Grapheme.splitIntoGraphemes(entry.chineseStr).map(y => Grapheme(y)).toSet
            val graphemesMissingFromConway: Set[Grapheme] = allGraphemes.filter(x => !conwayMap.contains(x)).toSet
            overlap.add(entry)
            overlapChars.add(eachLetter)
          }
        }
      }
      val wordGraphemes: Set[Grapheme] = Grapheme.splitIntoGraphemes(entry.chineseStr).map(y => Grapheme(y)).toSet
      val wordGrapehmesMissing: Set[Grapheme] = wordGraphemes.filter(x => !conwayMap.contains(x)).toSet
      if (wordGrapehmesMissing.size > 0) {
        val chnStr: String = entry.chineseStr
        val codesToShow: String = entry.codes.mkString(" ")
        otuputStrWithLies.append(chnStr).append(" ").append(codesToShow).append("\n")
        val test = ""
      }
    }
    overlapChars.size shouldBe 1
    overlapChars.toList(0) shouldBe 'z'
    val allGraphemes: Set[Grapheme] = overlap.map(x => Grapheme.splitIntoGraphemes(x.chineseStr).map(y => Grapheme(y))).flatten.toSet
    val graphemesMissingFromConway: Set[Grapheme] = allGraphemes.filter(x => !conwayMap.contains(x)).toSet

    overlap.size shouldBe 364 // there are many lines that contain z codes
  }

  private def lettersSansAscii(): Set[Char] = {
    val letters = ('a' to 'y').toSet
    return letters
  }

  it should "check the codes of cedict and conwaySansCedict" in {
    val conwaySansCedict: Set[OutputEntry] = OutputSorting.conwayOutSansCedict
    val cedictOut: Set[OutputEntry] = OutputSorting.cedictSetOut

    var overlap: mutable.Set[OutputEntry] = mutable.Set[OutputEntry]()
    var desiredChar: Option[OutputEntry] = None
    for (entry <- conwaySansCedict) {
      if (cedictOut.contains(entry)) {
        overlap.add(entry)
      }
      if (entry.chineseStr == "臒") {
        desiredChar = Some(entry)
      }
    }
    overlap.size shouldBe 0

    desiredChar should not be None

    desiredChar.get.codes shouldBe Set("ptfful", "pgfxul", "ptxful", "pgfful", "pgfl", "pgxl",
      "pgxful", "ptfvbl", "ptfl", "ptxl", "pgfvbl", "ptfxul")

    val test: String = ""
  }

}
