package de.rki.coronawarnapp.covidcertificate.validation.core

import android.os.Parcelable
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import kotlinx.android.parcel.Parcelize
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
        get() = when {
            isTechnicalFailure -> State.TECHNICAL_FAILURE
            hasPassed -> State.PASSED
            hasFailed -> State.FAILURE
            isOpen -> State.OPEN
            else -> State.OPEN
        }

    private val hasPassed: Boolean
        get() = expirationCheckPassed && jsonSchemaCheckPassed &&
            acceptanceRules.all { it.result == DccValidationRule.Result.PASSED } &&
            invalidationRules.all { it.result == DccValidationRule.Result.PASSED }

    private val isTechnicalFailure: Boolean
        get() = !expirationCheckPassed || !jsonSchemaCheckPassed

    private val isOpen: Boolean
        get() = expirationCheckPassed && jsonSchemaCheckPassed &&
            !acceptanceRules.any { it.result == DccValidationRule.Result.FAILED } &&
            !invalidationRules.any { it.result == DccValidationRule.Result.FAILED } &&
            (
                acceptanceRules.any { it.result == DccValidationRule.Result.OPEN } ||
                    invalidationRules.any { it.result == DccValidationRule.Result.OPEN }
                )

    private val hasFailed: Boolean
        get() = expirationCheckPassed && jsonSchemaCheckPassed &&
            (
                acceptanceRules.any { it.result == DccValidationRule.Result.FAILED } ||
                    invalidationRules.any { it.result == DccValidationRule.Result.FAILED }
                )

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
