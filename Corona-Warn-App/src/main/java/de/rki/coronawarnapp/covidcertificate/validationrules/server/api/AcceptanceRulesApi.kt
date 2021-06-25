package de.rki.coronawarnapp.covidcertificate.validationrules.server.api

import de.rki.coronawarnapp.covidcertificate.validationrules.rules.DgcValidationRule
import retrofit2.http.GET

interface AcceptanceRulesApi {

    @GET("/version/v1/ehn-dgc/acceptance-rules")
    suspend fun acceptanceRules(): Set<DgcValidationRule>
}
