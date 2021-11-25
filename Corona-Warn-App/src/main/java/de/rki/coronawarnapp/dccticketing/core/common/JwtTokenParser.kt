package de.rki.coronawarnapp.dccticketing.core.common

import com.nimbusds.jose.JOSEObject
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import javax.inject.Inject

class JwtTokenParser @Inject constructor(
    private val convertor: JwtTokenConverter
) {
    fun getAccessToken(jwt: String): DccTicketingAccessToken? =
        convertor.jsonToJwtToken(JOSEObject.split(jwt)[1].decodeToString())
}
