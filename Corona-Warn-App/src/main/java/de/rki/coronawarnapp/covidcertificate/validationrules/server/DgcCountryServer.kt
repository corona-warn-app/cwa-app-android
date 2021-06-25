package de.rki.coronawarnapp.covidcertificate.validationrules.server

import de.rki.coronawarnapp.covidcertificate.validationrules.country.DgcCountry
import de.rki.coronawarnapp.covidcertificate.validationrules.server.api.DgcCountryApi

class DgcCountryServer(
    private val dgcCountryApi: DgcCountryApi
) {

    suspend fun dgcCountries(): List<DgcCountry> = listOf() // TODO
}
