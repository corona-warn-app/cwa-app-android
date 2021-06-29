package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.result.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleRepository
import org.joda.time.DateTime
import javax.inject.Inject

class DccValidator @Inject constructor(
    dccValidationRuleRepository: DccValidationRuleRepository,
) {

    /**
     * Validates DCC against country of arrival's rules and issuer country rules
     */
    suspend fun validateDcc(
        arrivalCountries: Set<DccCountry>, // For future allow multiple country selection
        dateTime: DateTime,
        certificate: DccData<*>,
    ): DccValidation {
        throw NotImplementedError()
    }
}
