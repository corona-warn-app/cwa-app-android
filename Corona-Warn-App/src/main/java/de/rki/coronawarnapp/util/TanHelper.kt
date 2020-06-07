package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.ui.submission.TanConstants.MAX_LENGTH
import java.security.MessageDigest
import java.util.Locale

object TanHelper {
    fun isChecksumValid(tan: String): Boolean {
        if (tan.trim().length != MAX_LENGTH)
            return false
        val subTan = tan.substring(0, MAX_LENGTH - 1).toUpperCase(Locale.getDefault())
        val tanDigest = MessageDigest.getInstance("SHA-256").digest(subTan.toByteArray())
        var check_char = "%02x".format(tanDigest[0])[0]
        if (check_char == '0') check_char = 'G'
        if (check_char == '1') check_char = 'H'

        return check_char.toUpperCase() == tan.last().toUpperCase()
    }

    fun allCharactersValid(tan: String): Boolean {
        for (character in tan) {
            if (!isTanCharacterValid(character.toString()))
                return false
        }
    return true
    }

    fun isTanCharacterValid(character: String): Boolean {
        return "01OIL".contains(character).not()
    }
}
