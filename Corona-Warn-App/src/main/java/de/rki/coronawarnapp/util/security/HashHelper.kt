package de.rki.coronawarnapp.util.security

import java.security.MessageDigest

object HashHelper {
    fun hash256(input: String): String = MessageDigest
        .getInstance(SecurityConstants.DIGEST_ALGORITHM)
        .digest(input.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}
