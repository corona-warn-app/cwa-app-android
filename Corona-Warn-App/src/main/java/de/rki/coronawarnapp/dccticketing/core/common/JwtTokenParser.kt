package de.rki.coronawarnapp.dccticketing.core.common

import okio.ByteString.Companion.decodeBase64
import javax.inject.Inject

class JwtObject(val header: String, val body: String)

// TODO: could be improved in the following PR using JWT Library

class JwtTokenParser @Inject constructor() {
    fun parse(jwtToken: String): JwtObject? {
        val tokens = jwtToken.split('.')
        if (tokens.size < 2) return null
        val header = tokens[0].decodeBase64().toString()
        val body = tokens[1].decodeBase64().toString()
        return JwtObject(header, body)
    }
}
