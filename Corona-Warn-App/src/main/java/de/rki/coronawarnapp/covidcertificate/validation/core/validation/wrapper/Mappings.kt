package de.rki.coronawarnapp.covidcertificate.validation.core.validation.wrapper

import android.annotation.SuppressLint
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.EvaluatedDccRule
import dgca.verifier.app.engine.Result
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.Type
import org.joda.time.Instant
import org.json.JSONObject
import java.time.ZonedDateTime

internal fun assembleExternalParameter(
    certificate: DccData<*>,
    validationClock: Instant,
    countryCode: String
): ExternalParameter {
    return ExternalParameter(
        kid = "", // leave empty
        validationClock = validationClock.toZonedDateTime(),
        valueSets = emptyMap(), // todo mapping
        countryCode = countryCode,
        exp = certificate.header.expiresAt.toZonedDateTime(),
        iat = certificate.header.issuedAt.toZonedDateTime()
    )
}

internal val dgca.verifier.app.engine.ValidationResult.asEvaluatedDccRule: EvaluatedDccRule
    get() = EvaluatedDccRule(
        rule = this.rule.asDccValidationRule(),
        result = this.result.asDccValidationRuleResult
    )

internal val DccValidationRule.asExternalRule: Rule
    get() = Rule(
        identifier = identifier,
        type = typeDcc.asExternalType,
        version = version,
        schemaVersion = schemaVersion,
        engine = engine,
        engineVersion = engineVersion,
        certificateType = certificateType.asExternalCertificateType,
        descriptions = description,
        validFrom = validFrom.toZonedDateTime(),
        validTo = validTo.toZonedDateTime(),
        affectedString = affectedFields,
        logic = logic.asJsonNode,
        countryCode = country,
        region = null
    )

private val Result.asDccValidationRuleResult: DccValidationRule.Result
    get() = when (this) {
        Result.PASSED -> DccValidationRule.Result.PASSED
        Result.FAIL -> DccValidationRule.Result.FAILED
        Result.OPEN -> DccValidationRule.Result.OPEN
    }

private val ZonedDateTime.asExternalString: String
    get() = this.toString()

private fun Rule.asDccValidationRule() = DccValidationRule(
    identifier = identifier,
    typeDcc = type.asDccType,
    version = version,
    schemaVersion = schemaVersion,
    engine = engine,
    engineVersion = engineVersion,
    certificateType = certificateType.asInternalString,
    description = descriptions,
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
        CertificateType.GENERAL -> GENERAL
        CertificateType.TEST -> TEST
        CertificateType.VACCINATION -> VACCINATION
        CertificateType.RECOVERY -> RECOVERY
    }

private val String.asExternalCertificateType: CertificateType
    get() = when (this) {
        GENERAL -> CertificateType.GENERAL
        TEST -> CertificateType.TEST
        VACCINATION -> CertificateType.VACCINATION
        RECOVERY -> CertificateType.RECOVERY
        else -> throw IllegalArgumentException()
    }

private val JSONObject.asJsonNode: JsonNode
    get() = ObjectMapper().readTree(toString())

private val JsonNode.asJSONObject: JSONObject
    get() = JSONObject(toString())

@SuppressLint("NewApi")
private fun Instant.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(this.millis), UTC_ZONE_ID)
}

@SuppressLint("NewApi")
internal fun String.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.parse(this)
}

private const val GENERAL = "General"
private const val TEST = "Test"
private const val VACCINATION = "Vaccination"
private const val RECOVERY = "Recovery"
