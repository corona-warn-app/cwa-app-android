package de.rki.coronawarnapp.dccticketing.core.common

import okio.ByteString.Companion.decodeBase64
import javax.inject.Inject

class JwtObject(val header: String, val body: String)

class JwtTokenParser @Inject constructor() {
    fun parse(jwtToken: String): JwtObject {
        val tokens = jwtToken.split('.')
        val header = tokens[0].decodeBase64()?.toString() ?: throw NullPointerException("JWT has no header")
        val body = tokens[1].decodeBase64()?.toString() ?: throw NullPointerException("JWT has no body")
        return JwtObject(header, body)
    }
}
