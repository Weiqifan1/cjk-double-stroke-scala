package OutputTranslation

import ElementGenerator.ElementTranslateToAlphabet
import UtilityClasses.CharSystem.Junda
import UtilityClasses.{CedictEntry, Grapheme, OutputEntry, StaticFileCharInfoWithLetterConway}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import staticFileGenerators.SpecialCharacters.ReadSpecialCharacters
import staticFileGenerators.cedictMap.GenerateCedictMap

import scala.collection.{SortedMap, mutable}

class OutputSortingTest extends AnyFlatSpec with Matchers {

  // create a test that tests if there are any z characters in cedict or conway

  it should "test punctuation" in {
    
    val outJunda: SortedMap[String, List[OutputEntry]] = OutputSorting.mapFullJunda
    val outTzai: SortedMap[String, List[OutputEntry]] = OutputSorting.mapFullTzai
    
    val codesStartingWithZ: String = outJunda
      .filter(x => x._1.startsWith("z")).map(y => y._1 + " " + y._2.map(z => z.chineseStr).mkString(" ").trim).mkString("\n").trim
    
    codesStartingWithZ == 
    """z % ○
      |zc 3C
      |zng π日
      |zp 3P
      |zq 3Q
      |zz 88
      |zzgnu 21三體綜合症
      |zzgum 21三体综合症
      |zzih 95后
      |zzil 95後
      |zzz 421 996
      |zzzpi 502膠
      |zzzpo 502胶
      |zzzzq 2019冠状病毒病 2019冠狀病毒病""".stripMargin
    
  }

  it should "find any cedict or junda characters with z codes" in {
    val conwayOutFull: Set[OutputEntry] = OutputSorting.conFull
    //val cedictMap: Map[String, CedictEntry] = GenerateCedictMap.cedictMap
    val cedictOut: Set[OutputEntry] = OutputSorting.cedictSetOut
    val conwayMap: Map[Grapheme, StaticFileCharInfoWithLetterConway] = ElementTranslateToAlphabet.completeTranslatedConwayMap
    val lettersExceptZ = lettersSansAscii()

    val otuputStrWithLies: StringBuilder = new StringBuilder
    var overlap: mutable.Set[OutputEntry] = mutable.Set[OutputEntry]()
    var overlapChars: mutable.Set[Char] = mutable.Set[Char]()
    for (entry <- conwayOutFull) {
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

  it should "check the codes of cedict and conway" in {
    val conwayOutFull: Set[OutputEntry] = OutputSorting.conFull
    val cedictOut: Set[OutputEntry] = OutputSorting.cedictSetOut
    val conwayMap: Map[Grapheme, StaticFileCharInfoWithLetterConway] = ElementTranslateToAlphabet.completeTranslatedConwayMap


    var overlap: mutable.Set[OutputEntry] = mutable.Set[OutputEntry]()
    var desiredChar1: Option[OutputEntry] = None
    var desiredChar2: Option[OutputEntry] = None
    for (entry <- conwayOutFull) {
      if (entry.chineseStr == "臒") {
        desiredChar1 = Some(entry)
      }
    }
    desiredChar1 should not be None
    desiredChar1.get.codes shouldBe Set("ptfful", "pgfxul", "ptxful", "pgfful", "pgfl", "pgxl",
      "pgxful", "ptfvbl", "ptfl", "ptxl", "pgfvbl", "ptfxul")

    val test: String = ""
  }

}
