package de.rki.coronawarnapp.covidcertificate.validation.core.validation.wrapper

import android.annotation.SuppressLint
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.EvaluatedDccRule
import dgca.verifier.app.engine.Result
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.Type
import org.joda.time.Instant
import org.json.JSONObject
import java.time.ZonedDateTime

internal fun assembleExternalParameter(validationClock: Instant): dgca.verifier.app.engine.data.ExternalParameter {
    TODO()
}

val dgca.verifier.app.engine.ValidationResult.asEvaluatedDccRule: EvaluatedDccRule
    get() = EvaluatedDccRule(
        rule = this.rule.asDccValidationRule(),
        result = this.result.asDccValidationRuleResult
    )

private val Result.asDccValidationRuleResult: DccValidationRule.Result
    get() = when (this) {
        Result.PASSED -> DccValidationRule.Result.PASSED
        Result.FAIL -> DccValidationRule.Result.FAILED
        Result.OPEN -> DccValidationRule.Result.OPEN
    }

internal val DccValidationRule.asExternalRule: Rule
    get() = Rule(
        identifier = identifier,
        type = typeDcc.asExternalType,
        version = version,
        schemaVersion = schemaVersion,
        engine = engine,
        engineVersion = engineVersion,
        certificateType = certificateType.asExternalCertificateType,
        descriptions = description.first(), // TODO
        validFrom = validFrom.toZonedDateTime(),
        validTo = validTo.toZonedDateTime(),
        affectedString = affectedFields, // ??
        logic = logic.asJsonNode,
        countryCode = country,
        region = null
    )

@SuppressLint("NewApi")
fun String.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.parse(this)
}

val ZonedDateTime.asExternalString: String
    get() = this.toString()

fun Rule.asDccValidationRule() = DccValidationRule(
    identifier = identifier,
    typeDcc = type.asDccType,
    version = version,
    schemaVersion = schemaVersion,
    engine = engine,
    engineVersion = engineVersion,
    certificateType = certificateType.asInternalString,
    description = listOf(descriptions),
    validFrom = validFrom.asExternalString,
    validTo = validTo.asExternalString,
    affectedFields = affectedString,
    logic = logic.asJSONObject,
    country = countryCode,
)

private val Type.asDccType: DccValidationRule.Type
    get() = when (this) {
        Type.ACCEPTANCE -> DccValidationRule.Type.ACCEPTANCE
        Type.INVALIDATION -> DccValidationRule.Type.INVALIDATION
    }

private val DccValidationRule.Type.asExternalType: Type
    get() = when (this) {
        DccValidationRule.Type.ACCEPTANCE -> Type.ACCEPTANCE
        DccValidationRule.Type.INVALIDATION -> Type.INVALIDATION
    }

private val CertificateType.asInternalString: String
    get() = when (this) {
        CertificateType.GENERAL -> "General"
        CertificateType.TEST -> "Test"
        CertificateType.VACCINATION -> "Vaccination"
        CertificateType.RECOVERY -> "Recovery"
    }

private val String.asExternalCertificateType: CertificateType
    get() = when (this) {
        "General" -> CertificateType.GENERAL
        "Test" -> CertificateType.TEST
        "Vaccination" -> CertificateType.VACCINATION
        "Recovery" -> CertificateType.RECOVERY
        else -> throw IllegalArgumentException()
    }

private val JSONObject.asJsonNode: JsonNode
    get() {
        return ObjectMapper().createObjectNode()
    }

private val JsonNode.asJSONObject: JSONObject
    get() {
        return JSONObject(this.toString())
    }
