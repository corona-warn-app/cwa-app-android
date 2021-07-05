package de.rki.coronawarnapp.covidcertificate.validation.core

import android.os.Parcelable
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.business.EvaluatedDccRule
import kotlinx.parcelize.Parcelize
import org.joda.time.Instant

@Parcelize
data class DccValidation(
    val userInput: ValidationUserInput,
    val validatedAt: Instant,
    val expirationCheckPassed: Boolean,
    val jsonSchemaCheckPassed: Boolean,
    val acceptanceRules: Set<EvaluatedDccRule>,
    val invalidationRules: Set<EvaluatedDccRule>,
) : Parcelable {

    val state: State
        get() = State.PASSED // TODO

    enum class State {
        PASSED,
        OPEN,
        TECHNICAL_FAILURE,
        FAILURE,
    }
}

@Parcelize
data class ValidationUserInput(
    val arrivalCountry: DccCountry,
    val arrivalAt: Instant,
) : Parcelable
