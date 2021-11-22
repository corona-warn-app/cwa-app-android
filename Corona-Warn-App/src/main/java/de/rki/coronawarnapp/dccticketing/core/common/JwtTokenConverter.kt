package de.rki.coronawarnapp.dccticketing.core.common

import com.google.gson.Gson
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import javax.inject.Inject

@Reusable
class JwtTokenConverter @Inject constructor(
    @BaseGson private val gson: Gson
) {
    fun jsonToJwtToken(rawJson: String?): DccTicketingAccessToken? = rawJson?.let { gson.fromJson(it) }
    fun jsonToResultToken(rawJson: String): DccTicketingResultToken = gson.fromJson(rawJson)
}
