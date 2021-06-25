package de.rki.coronawarnapp.covidcertificate.validationrules.country

import de.rki.coronawarnapp.covidcertificate.validationrules.server.DgcCountryServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DgcCountryRepository @Inject constructor(
    dgcCountryServer: DgcCountryServer
) {
    val dgcCountries: Flow<List<DgcCountry>> = emptyFlow() // TODO
}
