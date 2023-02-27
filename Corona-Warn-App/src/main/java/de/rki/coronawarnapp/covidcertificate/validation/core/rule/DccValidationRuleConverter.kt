package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.BaseJackson
import javax.inject.Inject

@Reusable
class DccValidationRuleConverter @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper
) {
    fun jsonToRuleSet(rawJson: String?): List<DccValidationRule> = rawJson?.let { mapper.readValue(it) } ?: emptyList()
}
