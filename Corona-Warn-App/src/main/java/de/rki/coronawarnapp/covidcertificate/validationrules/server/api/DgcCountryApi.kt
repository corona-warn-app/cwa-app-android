package de.rki.coronawarnapp.covidcertificate.validationrules.server.api

import de.rki.coronawarnapp.covidcertificate.validationrules.country.DgcCountry
import retrofit2.http.GET

interface DgcCountryApi {

    @GET("/version/v1/ehn-dgc/onboarded-countries")
    suspend fun dgcCountries(): List<DgcCountry>
}
