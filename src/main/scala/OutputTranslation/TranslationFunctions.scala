package OutputTranslation

import UtilityClasses.ConwayUnambigous

import scala.collection.mutable
import TranslationHelpers.TranslationFunctions2
import TranslationHelpers.TranslationFunctions3

object TranslationFunctions {

  def translateVersionOne(unambigous: List[Set[ConwayUnambigous]]): Set[String] = {
    val mutableSet = mutable.Set[String]()
    unambigous.length match {
      case 1 => TranslationFunctions3.generateReadyCodeForOne(unambigous)
      case 2 => TranslationFunctions3.generateReadyCodeForTwo(unambigous)
      case 3 => TranslationFunctions3.generateReadyCodeForThree(unambigous)
      case 4 => TranslationFunctions3.generateReadyCodeForFour(unambigous)
      case 5 => TranslationFunctions3.generateReadyCodeForFive(unambigous)
      case n if n > 5 => TranslationFunctions3.generateReadyCodeOverSix(unambigous)
      case _ => throw IllegalArgumentException("translateVersionOne" + " does not have any codes")
    }
  }
/*
  def translateVersionTwo(unambigous: List[Set[ConwayUnambigous]]): Set[String] = {
    val mutableSet = mutable.Set[String]()
    unambigous.length match {
      case 1 => TranslationFunctions3.generateReadyCodeForOne(unambigous)
      case 2 => TranslationFunctions3.generateReadyCodeForTwo(unambigous)
      case 3 => TranslationFunctions3.generateReadyCodeForThree(unambigous)
      case 4 => TranslationFunctions3.generateReadyCodeForFour(unambigous)
      case n if n > 4 => TranslationFunctions3.generateReadyCodeForFive(unambigous)
      case _ => throw IllegalArgumentException("translateVersionOne" + " does not have any codes")
    }
  }*/
  
}
