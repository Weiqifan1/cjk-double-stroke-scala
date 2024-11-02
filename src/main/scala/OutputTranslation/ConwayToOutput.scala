package OutputTranslation

import ElementGenerator.{ElementAdjustedCodesNrTwo, ElementList, ElementTranslateToAlphabet, ElementType}
import UtilityClasses.{Cluster, Conway, ExtractsFromCedictCodes, Grapheme, StaticFileCharInfo, StaticFileCharInfoWithLetterConway}
import staticFileGenerators.JundaFrequency.JundaData
import staticFileGenerators.StaticFileGeneratorFacade
import staticFileGenerators.TzaiFrequency.TzaiData

import scala.collection.mutable

object ConwayToOutput {

  def rawConwayToOutputCodes(charOrWord: List[Grapheme]): Set[String] = {
    try{
      val mutableSet = mutable.Set[String]()
      charOrWord.length match {
        case 1 => generateCodesForSingleChars(charOrWord)
        case 2 => generateCodesForWordChars(charOrWord,
          List(ExtractsFromCedictCodes.FirstLast,
               ExtractsFromCedictCodes.FirstSecondLast), true ,5, 3)
        case 3 => generateCodesForWordChars(charOrWord,
          List(ExtractsFromCedictCodes.FirstOnly,
               ExtractsFromCedictCodes.FirstLast,
               ExtractsFromCedictCodes.FirstLast), true, 5, 0)
        case 4 => generateCodesForWordChars(charOrWord,
          List(ExtractsFromCedictCodes.FirstOnly,
               ExtractsFromCedictCodes.FirstOnly,
               ExtractsFromCedictCodes.FirstOnly,
               ExtractsFromCedictCodes.FirstLast), true, 5, 0)
        case n if n > 4 => generateCodesForWordChars(charOrWord,
          List(ExtractsFromCedictCodes.FirstOnly,
               ExtractsFromCedictCodes.FirstOnly,
               ExtractsFromCedictCodes.FirstOnly,
               ExtractsFromCedictCodes.FirstOnly,
               ExtractsFromCedictCodes.FirstOnly), true, 5, 0)
        //case _ => throw IllegalArgumentException("translateVersionOne" + " does not have any codes")
      }
    }
    catch
    {
      case e: Exception =>
        println(s"An error occurred: ${e.getMessage}")
        Set.empty[String]
    }
  }

  private def generateCodesForWordChars(charOrWord: List[Grapheme],
                                        extractRules: List[ExtractsFromCedictCodes],
                                        withElem: Boolean,
                                        fillWithZUntilLength: Int, createMiniCodes: Int): Set[String] = {
    val res: Set[String] = generateCodeWithExtract(charOrWord,
      extractRules,
      withElem,
      fillWithZUntilLength)
    val minicodes: Set[String] = res.map(x => x.take(createMiniCodes))
    val normalAndMini: Set[String] = res union minicodes
    val finalres = normalAndMini.filter(x => !(x.isEmpty)).toSet
    return finalres
  }

  private def generateCodesForSingleChars(charOrWord: List[Grapheme]): Set[String] = {
    val jundaNum: Option[JundaData] = charOrWord.head.junda
    val tzaiNum: Option[TzaiData] = charOrWord.head.tzai
    
    val lessthanFour = generateCodeWithExtract_noZFillup(charOrWord, List(ExtractsFromCedictCodes.FirstSecondThirdLast), true)
    val fourCodesWithz: Set[String] = generateCodeWithExtract(
      charOrWord, List(ExtractsFromCedictCodes.FirstSecondThirdLast), true, 4)
    val sixcodesWithZ: Set[String] = generateCodeWithExtract(
      charOrWord, List(ExtractsFromCedictCodes.FirstToFifthAndLast), false, 6)
    
    if ((jundaNum.isDefined && jundaNum.get.ordinal <= 5000) || 
        (tzaiNum.isDefined && tzaiNum.get.ordinal <= 5000)) {
      return fourCodesWithz union sixcodesWithZ union lessthanFour
    } else {
      return fourCodesWithz union sixcodesWithZ
    }
  }


  private def generateCodeWithExtract(charOrWord: List[Grapheme],
                              extractRules: List[ExtractsFromCedictCodes],
                              withElem: Boolean,
                              fillWithZUntilLength: Int): Set[String] = {
    val tempres: Set[String] = generateCodeWithExtract_noZFillup(charOrWord, extractRules, withElem)
    if (tempres.head == "toot" || tempres.head == "tooi") {
      val test = ""
    }
    val addZkeyFillup: Set[String] = fillUpWithZ(tempres, fillWithZUntilLength)
    return addZkeyFillup
  }

  private def fillUpWithZ(tempres: Set[String], fillWithZUntilLength: Int): Set[String] = {
    var res: mutable.Set[String] = mutable.Set[String]()
    for (each <- tempres) {
      if ( each.length < fillWithZUntilLength) {
        val numOfZ = fillWithZUntilLength - each.length
        val semires: String = each + ("z" * numOfZ).mkString
        res.add(semires)
      } else if (each.length == fillWithZUntilLength) {
        res.add(each)
      }
    }
    return res.toSet
  }

  private def generateCodeWithExtract_noZFillup(charOrWord: List[Grapheme],
                                      extractRules: List[ExtractsFromCedictCodes], withElem: Boolean): Set[String] = {
    assert(extractRules.length <= charOrWord.length, "extractRules should be less than charOrWord")
    var semires: List[Set[String]] = List()
    val minLength = math.min(charOrWord.length, extractRules.length)
    for (i <- 0 until minLength) {
      val charOrWordElement = charOrWord(i)
      val extractRuleElement = extractRules(i)
      val specificres = generateCodeWithExtractSpecific(charOrWordElement, extractRuleElement, withElem)
      semires = semires.appended(specificres)
    }
    val res: Set[String] = mergeNestedList(semires)
    return res
  }
  
  private def generateCodeWithExtractSpecific(charOnly: Grapheme, 
                                      extractRule: ExtractsFromCedictCodes, 
                                      withElem: Boolean): Set[String] = {
    var charInfoSet: Option[StaticFileCharInfo] = None

    try
      charInfoSet = Some(StaticFileGeneratorFacade.get(charOnly))
    catch
      case e: Exception => return Set("z")

    val elements = ElementList.elementTypes
    if (charOnly.char == "人") {
      val test = ""
    }
    var allRawStrokeCodes: String = charInfoSet.get.conwayColl.rawConway.rawConway
    var rawIds: List[Cluster] = charInfoSet.get.ids
    if (withElem) {
      val elem: (Option[String], String) = getElemFromraw(charOnly.char,  allRawStrokeCodes, rawIds, elements)
      if (elem._1.isDefined) {
        val rolledOutCodes: Set[String] = Conway.generateConway.expandAlternatives(elem._2)
        val resWithElem: Set[String] = generateAllLetters(charOnly, elem._1.get, rolledOutCodes, extractRule)
        return resWithElem
      } else {
        val rolledOutCodes: Set[String] = Conway.generateConway.expandAlternatives(allRawStrokeCodes)
        val resWithElem: Set[String] = generateAllLetters(charOnly, "", rolledOutCodes, extractRule)
        return resWithElem
      }
    }
    val rolledOutCodes: Set[String] = Conway.generateConway.expandAlternatives(allRawStrokeCodes)
    val resWithElem: Set[String] = generateAllLetters(charOnly, "", rolledOutCodes, extractRule)
    return resWithElem
  }

  private def generateAllLetters(charOnly: Grapheme,
                                 existingCodes: String,
                                 rolledOutCodes: Set[String],
                                 extractRule: ExtractsFromCedictCodes): Set[String] = {
    var output: mutable.Set[String] = mutable.Set()
    for (eachRolledUp <- rolledOutCodes) {
      val codelist: List[String] = if existingCodes.isEmpty then List() else List(existingCodes)
      val allLetters: List[String] = generateAllLettersEachRolledOut(charOnly, codelist, eachRolledUp, extractRule)
      val mergetranslated: String =  translatePrelimLettersAndStrokes(allLetters)
      output.add(mergetranslated)
    }
    return output.toSet
  }

  private def translatePrelimLettersAndStrokes(allLetters: List[String]): String = {
    var res: String = ""
    val translationMap: Map[String, String] = ElementTranslateToAlphabet.thirdChoiceMap
    for (letterAndPair <- allLetters) {
      if (translationMap.contains(letterAndPair)) {
        val translation: Option[String] = translationMap.get(letterAndPair)
        res = res + translation.get
      } else {
        throw new Exception("letter or pair not found in translationMap")
      }
    }
    return res
  }

  private def generateAllLettersEachRolledOut(charOnly: Grapheme,
                                              existingCodes: List[String],
                                              rolledOutCode: String,
                                              extractRule: ExtractsFromCedictCodes): List[String] = {
    if (existingCodes.length == extractRule.indices.length) {
      return existingCodes
    } else if (existingCodes.length > extractRule.indices.length) {
      throw new Exception("code list length larger than ExtractsFromCedictCodes")
    } else if (rolledOutCode.length == 0) {
      return existingCodes
    } else {
      if (rolledOutCode.length <= 2) {
        val res = existingCodes.appended(rolledOutCode)
        return res
      } else {
        val getExtractRuleIndex: Int = extractRule.indices(existingCodes.length)
        val nextTwo: String = takeFrontOrBack(rolledOutCode, 2, getExtractRuleIndex > -0)
        val updatedexistingCodes: List[String] = existingCodes.appended(nextTwo)
        val updatedrolledOutCode: String = dropFrontOrBack(rolledOutCode, 2, getExtractRuleIndex > 0 )
        return generateAllLettersEachRolledOut(charOnly, updatedexistingCodes, updatedrolledOutCode, extractRule)
      }
    }
  }

  private def takeFrontOrBack(str: String, amount: Int, fromFront: Boolean): String =
    if fromFront then str.take(amount) else str.takeRight(amount)

  private def dropFrontOrBack(str: String, amount: Int, fromFront: Boolean): String =
    if fromFront then str.drop(amount) else str.dropRight(amount)


  private def getElemFromraw(charorWord: String,
                             rawConway: String,
                             idsWithShapes: List[Cluster],
                             elements: Set[ElementType]): (Option[String], String) = {
    if (charorWord == "問" || charorWord == "話") {
      val test = ""
    }
    var tuppleList: List[(Option[String], String)] = List()
    for (elem <- elements) {
      val conwayStart: Boolean = rawConway.startsWith(elem.elementVersions)
      val idsStart: Boolean = idsStartWithElem(idsWithShapes, elem.elementCodes)
      if (conwayStart && idsStart) {
        val remainingStrokes: String = rawConway.substring(elem.elementVersions.length, rawConway.length)
        val elemLetter: String = elem.elementKeyLetter
        tuppleList = tuppleList.appended((Some(elemLetter), remainingStrokes))
      }
    }
    if (tuppleList.size == 1) {
      return (tuppleList.head._1, tuppleList.head._2)
    } else if (tuppleList.size == 0) {
      return (None, rawConway)
    } else {
      throw new Exception("more than one Element found in raw conway")
    }
  }

  private def idsStartWithElem(clusters: List[Cluster], cluster: Cluster): Boolean = {
    var foundClusterMatch: Boolean = false
    for (eachCluster <- clusters) {
      val currentClusterMatch: Boolean = idsStartWithCluster(eachCluster, cluster)
      if (currentClusterMatch) {
        foundClusterMatch = true;
      }
    }
    return foundClusterMatch;
  }

  private def idsStartWithCluster(
                                   eachCharIdsCluster: Cluster,
                                   elementCluster: Cluster
                                 ): Boolean = {
    var CharClusterGraphemes: List[Grapheme] =
      eachCharIdsCluster.graphemes
    var elementClusterGraphemes: List[Grapheme] =
      elementCluster.graphemes

    def doesSublistExistWithShapeCondition(
                                            list1: List[Grapheme],
                                            list2: List[Grapheme]
                                          ): Boolean = {
      if (list2.isEmpty) {
        return true // An empty list is considered a sublist of any list
      }
      if (list1.isEmpty) {
        return false // A non-empty list2 cannot be a sublist of an empty list1
      }

      val sublistLength = list2.length

      for (i <- list1.indices) {
        // Check if there's enough room for list2 to be a sublist of list1 starting at i
        if (i + sublistLength <= list1.length) {
          val sublistCandidate = list1.slice(i, i + sublistLength)
          if (sublistCandidate == list2) {
            // list2 matches this position in list1, now check the preceding elements
            val precedingElements = list1.slice(0, i)
            val allPrecedingShapes = precedingElements.forall(_.isShape)
            if (allPrecedingShapes) {
              return true
            }
          }
        }
      }
      false
    }

    // Use the helper function to check for the sublist condition
    doesSublistExistWithShapeCondition(CharClusterGraphemes, elementClusterGraphemes)
  }
  
  private def mergeNestedList(listOfSets: List[Set[String]]): Set[String] = {
    def helper(remaining: List[Set[String]], current: Set[String]): Set[String] = {
      remaining match {
        case Nil => current
        case head :: tail =>
          for {
            elem <- head
            combination <- helper(tail, if (current.isEmpty) Set(elem) else current.map(_ + elem))
          } yield combination
      }
    }
    helper(listOfSets, Set.empty[String])
  }

}