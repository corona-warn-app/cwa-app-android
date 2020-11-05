package de.rki.coronawarnapp.ui.submission.tan

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TanTest : BaseTest() {

    @Test
    fun isValidCharacter() {
        // valid
        val validCharacters = arrayOf(
            "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
            "J", "K", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        )
        for (character in validCharacters) {
            Tan.isTanCharacterValid(character) shouldBe true
        }

        // invalid
        val invalidCharacters = arrayOf(
            "0", "1", "O", "L", "I", "Ö", "*", "&", "-", "a", "b",
            "c", "ö", "ß", "é", ".", " ", "€", "(", ")", ";", ","
        )
        for (character in invalidCharacters) {
            Tan.isTanCharacterValid(character) shouldBe false
        }
    }

    @Test
    fun areCharactersValid() {
        // valid input strings (not necessarily valid TANs)
        val validStrings = arrayOf(
            "ABCD", "2345", "PTPHM35RP4", "AAAAAAAAAA", "BBBBB"
        )
        for (text in validStrings) {
            Tan.allCharactersValid(text) shouldBe true
        }

        // invalid input strings
        val invalidStrings = arrayOf(
            "ABCDÖ", "01234", "PTPHM15RP4", "AAAAAA AAA", "BB.BBB"
        )
        for (text in invalidStrings) {
            Tan.allCharactersValid(text) shouldBe false
        }
    }

    @Test
    fun isChecksumValid() {
        // valid
        val validTans = arrayOf(
            "9A3B578UMG", "DEU7TKSV3H", "PTPHM35RP4", "V923D59AT8", "H9NC5CQ34E"
        )
        for (tan in validTans) {
            Tan.isChecksumValid(tan) shouldBe true
        }

        // invalid
        val invalidTans = arrayOf(
            "DEU7TKSV32", "DEU7TKSV33", "DEU7TKSV34", "DEU7TKSV35",
            "DEU7TKSV36", "DEU7TKSV37", "DEU7TKSV38", "DEU7TKSV39",
            "DEU7TKSV3A", "DEU7TKSV3B", "DEU7TKSV3C", "DEU7TKSV3D",
            "DEU7TKSV3E", "DEU7TKSV3F", "DEU7TKSV3G",
            " QV5FQ38MA",
            "9A3B578UM0", "DEU7TKSV31", "Q4XBJCB43", "929B96CA8"
        )
        for (tan in invalidTans) {
            Tan.isChecksumValid(tan) shouldBe false
        }
    }
}
