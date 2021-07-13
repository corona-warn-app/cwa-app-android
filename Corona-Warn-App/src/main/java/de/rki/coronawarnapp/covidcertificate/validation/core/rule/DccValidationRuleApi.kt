package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface DccValidationRuleApi {

    @GET("/version/v1/ehn-dgc/acceptance-rules")
    suspend fun acceptanceRules(): Response<ResponseBody>

    @GET("/version/v1/ehn-dgc/invalidation-rules")
    suspend fun invalidationRules(): Response<ResponseBody>
}
