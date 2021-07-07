package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.business.BusinessValidator
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.technical.TechnicalValidator
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

class DccValidator @Inject constructor(
    private val technicalValidator: TechnicalValidator,
    private val businessValidator: BusinessValidator,
) {

    /**
     * Validates DCC against country of arrival's rules and issuer country rules
     */
    suspend fun validateDcc(
        userInput: ValidationUserInput,
        certificate: DccData<*>,
    ): DccValidation {
        Timber.tag(TAG).v("validateDcc(userInput=%s)", userInput)

        val technicalValidation = technicalValidator.validate(
            userInput.arrivalAt, certificate
        )

        val businessValidation = businessValidator.validate(
            setOf(userInput.arrivalCountry),
            userInput.arrivalAt,
            certificate
        )

        return DccValidation(
            validatedAt = Instant.now(),
            userInput = userInput,
            expirationCheckPassed = true, // TODO
            jsonSchemaCheckPassed = true, // TODO use DccJsonSchemaValidator
            acceptanceRules = businessValidation.acceptanceRules,
            invalidationRules = businessValidation.invalidationRules
        )
    }

    companion object {
        private const val TAG = "DccValidator"
    }
}
