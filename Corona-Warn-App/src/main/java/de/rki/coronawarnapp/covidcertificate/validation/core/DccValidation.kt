package de.rki.coronawarnapp.covidcertificate.validation.core

import android.os.Parcelable
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.LocalDateTime

@Parcelize
data class DccValidation(
    val userInput: ValidationUserInput,
    val validatedAt: Instant,
    val signatureCheckPassed: Boolean,
    val expirationCheckPassed: Boolean,
    val jsonSchemaCheckPassed: Boolean,
    val acceptanceRules: Set<EvaluatedDccRule>,
    val invalidationRules: Set<EvaluatedDccRule>,
) : Parcelable {

    @IgnoredOnParcel
    val rules: Set<EvaluatedDccRule> = acceptanceRules + invalidationRules

    val state: State
        get() = when {
            isTechnicalFailure -> State.TECHNICAL_FAILURE
            hasPassed -> State.PASSED
            hasFailed -> State.FAILURE
            else -> State.OPEN
        }

    private val hasPassed: Boolean
        get() = expirationCheckPassed && jsonSchemaCheckPassed && signatureCheckPassed &&
            acceptanceRules.all { it.result == DccValidationRule.Result.PASSED } &&
            invalidationRules.all { it.result == DccValidationRule.Result.PASSED }

    private val isTechnicalFailure: Boolean
        get() = !expirationCheckPassed || !jsonSchemaCheckPassed || !signatureCheckPassed

    private val hasFailed: Boolean
        get() = expirationCheckPassed && jsonSchemaCheckPassed && signatureCheckPassed &&
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
    val arrivalDateTime: LocalDateTime,
) : Parcelable
