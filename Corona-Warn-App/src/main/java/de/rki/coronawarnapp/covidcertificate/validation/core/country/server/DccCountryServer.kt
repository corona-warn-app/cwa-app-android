package de.rki.coronawarnapp.covidcertificate.validation.core.country.server

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry

class DccCountryServer(
    private val dccCountryApi: DccCountryApi
) {

    suspend fun dccCountries(): List<DccCountry> = listOf() // TODO
}
