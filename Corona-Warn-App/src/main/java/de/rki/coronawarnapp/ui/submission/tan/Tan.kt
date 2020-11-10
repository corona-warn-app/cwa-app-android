package de.rki.coronawarnapp.ui.submission.tan

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

data class Tan(
    val value: String
) {

    val isCorrectLength = value.length == MAX_LENGTH
    val areCharactersValid = allCharactersValid(value)
    val isTanValidFormat = isCorrectLength && isChecksumValid(value)
    val isTanValid = areCharactersValid && isTanValidFormat

    companion object {
        const val MAX_LENGTH = 10
        internal val ALPHA_NUMERIC_CHARS = ('a'..'z').plus('A'..'Z').plus('0'..'9')

        private const val VALID_CHARACTERS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"

        fun isChecksumValid(tan: String): Boolean {
            if (tan.trim().length != MAX_LENGTH)
                return false
            val subTan = tan.substring(0, MAX_LENGTH - 1).toUpperCase(Locale.ROOT)
            val tanDigest = MessageDigest.getInstance("SHA-256")
                .digest(subTan.toByteArray(StandardCharsets.US_ASCII))
            var checkChar = "%02x".format(tanDigest[0])[0]
            if (checkChar == '0') checkChar = 'G'
            if (checkChar == '1') checkChar = 'H'

            return checkChar.toUpperCase() == tan.last().toUpperCase()
        }

        fun allCharactersValid(tan: String): Boolean {
            for (character in tan) {
                if (!isTanCharacterValid(character.toString()))
                    return false
            }
            return true
        }

        fun isTanCharacterValid(character: String): Boolean {
            return VALID_CHARACTERS.contains(character)
        }
    }
}
