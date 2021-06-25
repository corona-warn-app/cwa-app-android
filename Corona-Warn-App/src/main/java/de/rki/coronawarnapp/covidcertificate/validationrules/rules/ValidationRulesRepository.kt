package de.rki.coronawarnapp.covidcertificate.validationrules.rules

import de.rki.coronawarnapp.covidcertificate.validationrules.country.DgcCountry
import de.rki.coronawarnapp.covidcertificate.validationrules.server.DgcValidationRulesServer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ValidationRulesRepository @Inject constructor(
    dgcValidationRulesServer: DgcValidationRulesServer
) {

    /**
     * Gets validation rules of the arrival country
     *
     */
    suspend fun validationRules(arrivalCountry: DgcCountry): List<DgcValidationRule> {
        return emptyList() // TODO
    }
}
