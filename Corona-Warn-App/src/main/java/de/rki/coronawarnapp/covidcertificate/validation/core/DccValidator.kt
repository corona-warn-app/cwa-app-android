package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.business.BusinessValidator
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.technical.TechnicalValidator
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

class DccValidator @Inject constructor(
    private val technicalValidator: TechnicalValidator,
    private val businessValidator: BusinessValidator
) {

    /**
     * Validates DCC against country of arrival's rules and issuer country rules
     */
    suspend fun validateDcc(
        arrivalCountries: Set<DccCountry>, // For future allow multiple country selection
        validationClock: Instant,
        certificate: DccData<*>,
    ): DccValidation {
        Timber.tag(TAG).v("validateDcc(countries=%s)", arrivalCountries)

        val technicalValidation = technicalValidator.validate(
            validationClock, certificate
        )
        val businessValidation = businessValidator.validate(
            arrivalCountries,
            validationClock,
            certificate
        )

        return DccValidation(technicalValidation, businessValidation)
    }

    companion object {
        private val TAG = "DccValidator"
    }
}
