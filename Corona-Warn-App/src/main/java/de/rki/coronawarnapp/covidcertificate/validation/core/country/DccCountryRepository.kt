package de.rki.coronawarnapp.covidcertificate.validation.core.country

import de.rki.coronawarnapp.covidcertificate.validation.core.country.server.DccCountryServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccCountryRepository @Inject constructor(
    dccCountryServer: DccCountryServer
) {
    val dccCountries: Flow<List<DccCountry>> = emptyFlow() // TODO
}
