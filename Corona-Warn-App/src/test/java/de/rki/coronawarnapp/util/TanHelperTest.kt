package de.rki.coronawarnapp.util

import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test

class TanHelperTest {

    @Test
    fun isValidCharacter() {
        // valid
        val validCharacters = arrayOf(
            "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
            "J", "K", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        )
        for (character in validCharacters) {
            MatcherAssert.assertThat(
                TanHelper.isTanCharacterValid(character),
                CoreMatchers.equalTo(true)
            )
        }

        // invalid
        val invalidCharacters = arrayOf(
            "0", "1", "O", "L", "I", "Ö", "*", "&", "-", "a", "b",
            "c", "ö", "ß", "é", ".", " ", "€", "(", ")", ";", ","
        )
        for (character in invalidCharacters) {
            MatcherAssert.assertThat(
                TanHelper.isTanCharacterValid(character),
                CoreMatchers.equalTo(false)
            )
        }
    }

    @Test
    fun areCharactersValid() {
        // valid input strings (not necessarily valid TANs)
        val validStrings = arrayOf(
            "ABCD", "2345", "PTPHM35RP4", "AAAAAAAAAA", "BBBBB"
        )
        for (text in validStrings) {
            MatcherAssert.assertThat(
                TanHelper.allCharactersValid(text),
                CoreMatchers.equalTo(true)
            )
        }

        // invalid input strings
        val invalidStrings = arrayOf(
            "ABCDÖ", "01234", "PTPHM15RP4", "AAAAAA AAA", "BB.BBB"
        )
        for (text in invalidStrings) {
            MatcherAssert.assertThat(
                TanHelper.allCharactersValid(text),
                CoreMatchers.equalTo(false)
            )
        }
    }

    @Test
    fun isChecksumValid() {
        // valid
        val validTans = arrayOf(
            "9A3B578UMG", "DEU7TKSV3H", "PTPHM35RP4", "V923D59AT8", "H9NC5CQ34E"
        )
        for (tan in validTans) {
            MatcherAssert.assertThat(
                TanHelper.isChecksumValid(tan),
                CoreMatchers.equalTo(true)
            )
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
            MatcherAssert.assertThat(
                TanHelper.isChecksumValid(tan),
                CoreMatchers.equalTo(false)
            )
        }
    }
}
