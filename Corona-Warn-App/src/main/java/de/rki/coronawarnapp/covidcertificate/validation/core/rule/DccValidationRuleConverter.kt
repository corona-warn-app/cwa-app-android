package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import com.google.gson.Gson
import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import javax.inject.Inject

@Reusable
class DccValidationRuleConverter @Inject constructor(
    @BaseGson private val gson: Gson
) {
    fun jsonToRuleSet(rawJson: String?): List<DccValidationRule> = rawJson?.let { gson.fromJson(it) } ?: emptyList()
}
