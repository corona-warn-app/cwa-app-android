package de.rki.coronawarnapp.covidcertificate.validation.core.rule.server

import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import retrofit2.http.GET

interface DccValidationRuleApi {

    @GET("/version/v1/ehn-dgc/acceptance-rules")
    suspend fun acceptanceRules(): Set<DccValidationRule>

    @GET("/version/v1/ehn-dgc/invalidation-rules")
    suspend fun invalidationRules(): Set<DccValidationRule>
}
