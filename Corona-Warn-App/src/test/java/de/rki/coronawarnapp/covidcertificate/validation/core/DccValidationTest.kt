package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.createDccRule
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTime
import dgca.verifier.app.engine.data.RuleCertificateType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.ZoneOffset

class DccValidationTest : BaseTest() {

    private val nowUTC = Instant.ofEpochSecond(1625827095)

    private val userInput = ValidationUserInput(
        DccCountry("PT"),
        nowUTC.toLocalDateTime(ZoneOffset.UTC),
    )

    @Test
    fun `validation state passed`() {
        val rule1 = getRule(DccValidationRule.Result.PASSED)
        val rule2 = getRule(DccValidationRule.Result.PASSED)
        val rule3 = getRule(DccValidationRule.Result.PASSED)
        val rule4 = getRule(DccValidationRule.Result.PASSED)
        DccValidation(
            userInput,
            nowUTC,
            signatureCheckPassed = true,
            expirationCheckPassed = true,
            jsonSchemaCheckPassed = true,
            acceptanceRules = setOf(rule1, rule2),
            invalidationRules = setOf(rule3, rule4),
        ).state shouldBe DccValidation.State.PASSED
    }

    @Test
    fun `validation state failure`() {
        val rule1 = getRule(DccValidationRule.Result.PASSED)
        val rule2 = getRule(DccValidationRule.Result.FAILED)
        val rule3 = getRule(DccValidationRule.Result.OPEN)
        val rule4 = getRule(DccValidationRule.Result.PASSED)
        DccValidation(
            userInput,
            nowUTC,
            signatureCheckPassed = true,
            expirationCheckPassed = true,
            jsonSchemaCheckPassed = true,
            acceptanceRules = setOf(rule1),
            invalidationRules = setOf(rule2, rule3, rule4),
        ).state shouldBe DccValidation.State.FAILURE
    }

    @Test
    fun `validation state technical failure`() {
        val rule1 = getRule(DccValidationRule.Result.PASSED)
        val rule2 = getRule(DccValidationRule.Result.OPEN)
        DccValidation(
            userInput,
            nowUTC,
            signatureCheckPassed = true,
            expirationCheckPassed = true,
            jsonSchemaCheckPassed = false,
            acceptanceRules = setOf(rule1),
            invalidationRules = setOf(rule2),
        ).state shouldBe DccValidation.State.TECHNICAL_FAILURE
    }

    @Test
    fun `validation state technical failure 2`() {
        val rule1 = getRule(DccValidationRule.Result.PASSED)
        val rule2 = getRule(DccValidationRule.Result.FAILED)
        val rule3 = getRule(DccValidationRule.Result.PASSED)
        val rule4 = getRule(DccValidationRule.Result.PASSED)
        DccValidation(
            userInput,
            nowUTC,
            signatureCheckPassed = true,
            expirationCheckPassed = false,
            jsonSchemaCheckPassed = true,
            acceptanceRules = setOf(rule1, rule3),
            invalidationRules = setOf(rule2, rule4),
        ).state shouldBe DccValidation.State.TECHNICAL_FAILURE
    }

    @Test
    fun `validation state technical failure 3`() {
        val rule1 = getRule(DccValidationRule.Result.PASSED)
        val rule2 = getRule(DccValidationRule.Result.FAILED)
        val rule3 = getRule(DccValidationRule.Result.PASSED)
        val rule4 = getRule(DccValidationRule.Result.PASSED)
        DccValidation(
            userInput,
            nowUTC,
            signatureCheckPassed = false,
            expirationCheckPassed = true,
            jsonSchemaCheckPassed = true,
            acceptanceRules = setOf(rule1, rule3),
            invalidationRules = setOf(rule2, rule4),
        ).state shouldBe DccValidation.State.TECHNICAL_FAILURE
    }

    @Test
    fun `validation state open`() {
        val rule1 = getRule(DccValidationRule.Result.PASSED)
        val rule2 = getRule(DccValidationRule.Result.OPEN)
        val rule3 = getRule(DccValidationRule.Result.PASSED)
        val rule4 = getRule(DccValidationRule.Result.PASSED)
        DccValidation(
            userInput,
            nowUTC,
            signatureCheckPassed = true,
            expirationCheckPassed = true,
            jsonSchemaCheckPassed = true,
            acceptanceRules = setOf(rule1, rule3),
            invalidationRules = setOf(rule2, rule4),
        ).state shouldBe DccValidation.State.OPEN
    }

    private fun getRule(result: DccValidationRule.Result) =
        EvaluatedDccRule(
            createDccRule(certificateType = RuleCertificateType.GENERAL),
            result
        )
}
