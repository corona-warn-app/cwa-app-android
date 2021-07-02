package de.rki.coronawarnapp.covidcertificate.validation.core.country

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface DccCountryApi {

    @GET("/version/v1/ehn-dgc/onboarded-countries")
    suspend fun onboardedCountries(): Response<ResponseBody>
}
