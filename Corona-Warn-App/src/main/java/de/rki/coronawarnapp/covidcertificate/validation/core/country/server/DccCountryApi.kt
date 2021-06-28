package de.rki.coronawarnapp.covidcertificate.validation.core.country.server

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import retrofit2.http.GET

interface DccCountryApi {

    @GET("/version/v1/ehn-dgc/onboarded-countries")
    suspend fun dgcCountries(): List<DccCountry>
}
