package de.rki.coronawarnapp.covidcertificate.validationrules.server.api

import de.rki.coronawarnapp.covidcertificate.validationrules.rules.DgcValidationRule
import retrofit2.http.GET

interface DgcInvalidationRulesApi {
    @GET("/version/v1/ehn-dgc/invalidation-rules")
    suspend fun invalidationRules(): Set<DgcValidationRule>
}
