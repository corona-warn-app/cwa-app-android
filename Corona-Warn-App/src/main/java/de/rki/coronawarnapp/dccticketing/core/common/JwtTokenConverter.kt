package de.rki.coronawarnapp.dccticketing.core.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.util.serialization.BaseJackson
import javax.inject.Inject

@Reusable
class JwtTokenConverter @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper
) {
    fun jsonToJwtToken(rawJson: String): DccTicketingAccessToken = mapper.readValue(rawJson)
    fun jsonToResultToken(rawJson: String): DccTicketingResultToken = mapper.readValue(rawJson)
}
