package OutputTranslation

import ElementGenerator.{ElementList, ElementType}
import UtilityClasses.CharSystem.{Junda, Tzai}
import UtilityClasses.{CedictEntry, CharSystem, Grapheme, OutputEntry}
import staticFileGenerators.Conway.GenerateConwayCodes
import staticFileGenerators.SpecialCharacters.ReadSpecialCharacters
import staticFileGenerators.cedictMap.GenerateCedictMap

import scala.math.Ordering.Implicits.*
import scala.jdk.StreamConverters.*
import UtilityClasses.{CharSystem, OutputEntry}
import OutputEntryOrdering.*

import scala.collection.immutable.SortedMap
import scala.collection.mutable

class OutputSorting {

  // Method to provide custom ordering
  def customOrdering: Ordering[String] = new Ordering[String] {
    override def compare(x: String, y: String): Int = {
      val lengthCompare = x.length.compare(y.length)
      if (lengthCompare != 0) lengthCompare else x.compare(y)
    }
  }

  def codeToOutputEntry(input: Set[OutputEntry]): mutable.SortedMap[String, Set[OutputEntry]] = {
    implicit val ordering: Ordering[String] = customOrdering
    val sortedMap: mutable.SortedMap[String, Set[OutputEntry]] = mutable.SortedMap[String, Set[OutputEntry]]()

    for (eachEntry <- input) {
      for (code <- eachEntry.codes) {
        val updatedSet = sortedMap.getOrElse(code, Set()) + eachEntry
        sortedMap.update(code, updatedSet)
      }
    }
    sortedMap
  }

  def mapFromOutput(input: List[List[OutputEntry]], charSystem: CharSystem): SortedMap[String, List[OutputEntry]] = {
    implicit val ordering: Ordering[String] = customOrdering
    var res: mutable.SortedMap[String, List[OutputEntry]] = mutable.SortedMap[String, List[OutputEntry]]()

    val merge: Set[OutputEntry] = mergeOutputEntries(input)
    val codeToOutput: mutable.SortedMap[String, Set[OutputEntry]] = codeToOutputEntry(merge)

    val test = ""
    for ((myKey, myVal) <- codeToOutput) {
      val sortedVal: List[OutputEntry] = OutputEntryOrdering.sortSetOfOutput(myVal, charSystem)
      if (sortedVal.size > 10) {
        val test = ""
      }
      res.addOne((myKey, sortedVal))
    }
    SortedMap.from(res)(ordering)
  }

  def mergeOutputEntries(input: List[List[OutputEntry]]): Set[OutputEntry] = {
    val res: mutable.Map[String, List[OutputEntry]] = mutable.Map()

    // Merge entries by their `chineseStr`
    for (collOfEntry <- input; entry <- collOfEntry) {
      res.updateWith(entry.chineseStr) {
        case Some(existingEntries) => Some(existingEntries :+ entry)
        case None => Some(List(entry))
      }
    }

    val finalRes: mutable.Set[OutputEntry] = mutable.Set()
    // Create new merged entries based on the grouped values
    for ((chineseStr, eachList) <- res) {
      if (eachList.size == 1) {
        finalRes.add(eachList.head)
      } else {
        var updatedMeaning = ""
        var updatedPron = ""
        var updatedTradSimp = ""
        var updatedJundaReverseOrder: List[Grapheme] = List()
        var updatedTzaiReverseOrder: List[Grapheme] = List()
        var updatedCodes: Set[String] = Set()

        for (ent <- eachList) {
          if (updatedMeaning.isEmpty || ent.meaning.nonEmpty) {
            updatedMeaning = ent.meaning
          }
          if (updatedPron.isEmpty || ent.pron.nonEmpty) {
            updatedPron = ent.pron
          }
          if (updatedTradSimp.isEmpty || ent.tradSimp.nonEmpty) {
            updatedTradSimp = ent.tradSimp
          }
          if (updatedJundaReverseOrder.isEmpty || ent.jundaReverseOrder.nonEmpty) {
            updatedJundaReverseOrder = ent.jundaReverseOrderG
          }
          if (updatedTzaiReverseOrder.isEmpty || ent.tzaiReverseOrder.nonEmpty) {
            updatedTzaiReverseOrder = ent.tzaiReverseOrderG
          }
          updatedCodes = updatedCodes ++ ent.codes
        }

        val outputNewEntry = OutputEntry(
          chineseStr,
          updatedMeaning,
          updatedPron,
          updatedTradSimp,
          updatedJundaReverseOrder,
          updatedTzaiReverseOrder,
          updatedCodes
        )
        finalRes.add(outputNewEntry)
      }
    }
    finalRes.toSet
  }

  def getStringsFromElements(elems: Set[ElementType]): Set[String] = {
    val res: Set[String] = elems.map(x => x.rawString)
    if (res != null && res.nonEmpty) {
      return res
    } else {
      throw new Exception("No elements found")
    }
  }

}

object OutputSorting {
  val outClass = new OutputTranslation()
  val outSorting = new OutputSorting()
  val elements: Set[String] = outSorting.getStringsFromElements(ElementList.elementTypes)
  val cedictSet: Set[CedictEntry] = GenerateCedictMap.cedictCompleteSet
  val cedictSetOut: Set[OutputEntry] = OutputTranslation.outputCedict
  val conFull: Set[OutputEntry] = OutputTranslation.outputConway //OutputTranslation.conwayOutFull
  val specialChars: List[OutputEntry] = ReadSpecialCharacters.allCharacterOutput
  val mapFullJunda: SortedMap[String, List[OutputEntry]] = outSorting.mapFromOutput(
    List(conFull.toList, cedictSetOut.toList), Junda)
  val mapFullTzai: SortedMap[String, List[OutputEntry]] = outSorting.mapFromOutput(
    List(conFull.toList, cedictSetOut.toList), Tzai)
  //yveo 449 称 449, 颓 2996, 頺 8820, 稧 8851, 秼 9416, 龝 9710, 稴 9748, 頽 2147483647, 棃 2147483647, 穕 2147483647
  
  val test = mapFullTzai.get("yveo")
  val test2 = mapFullTzai.get("aroz")
  val tes2 = ""
}
















