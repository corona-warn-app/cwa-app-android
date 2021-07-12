package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import dgca.verifier.app.engine.Result
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.Type
import org.joda.time.Instant
import java.time.ZonedDateTime

internal fun assembleExternalParameter(
    certificate: DccData<*>,
    validationClock: Instant,
    countryCode: String,
    valueSets: Map<String, List<String>>,
): ExternalParameter {
    return ExternalParameter(
        kid = certificate.kid,
        validationClock = validationClock.toZonedDateTime(),
        valueSets = valueSets,
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
        type = type.asExternalType,
        version = version,
        schemaVersion = schemaVersion,
        engine = engine,
        engineVersion = engineVersion,
        certificateType = certificateType.asExternalCertificateType,
        descriptions = description.asMap,
        validFrom = validFrom.toZonedDateTime(),
        validTo = validTo.toZonedDateTime(),
        affectedString = affectedFields,
        logic = logic,
        countryCode = country,
        region = null // leave empty
    )

private val Result.asDccValidationRuleResult: DccValidationRule.Result
    get() = when (this) {
        Result.PASSED -> DccValidationRule.Result.PASSED
        Result.FAIL -> DccValidationRule.Result.FAILED
        Result.OPEN -> DccValidationRule.Result.OPEN
    }

private fun Rule.asDccValidationRule() = DccValidationRule(
    identifier = identifier,
    type = type.asInternalType,
    version = version,
    schemaVersion = schemaVersion,
    engine = engine,
    engineVersion = engineVersion,
    certificateType = certificateType.asInternalString,
    description = descriptions.asList,
    validFrom = validFrom.asExternalString,
    validTo = validTo.asExternalString,
    affectedFields = affectedString,
    logic = logic,
    country = countryCode,
)

private val Type.asDccType: DccValidationRule.Type
    get() = when (this) {
        Type.ACCEPTANCE -> DccValidationRule.Type.ACCEPTANCE
        Type.INVALIDATION -> DccValidationRule.Type.INVALIDATION
    }

private val Type.asInternalType: String
    get() = when (this) {
        Type.ACCEPTANCE -> "Acceptance"
        Type.INVALIDATION -> "Invalidation"
    }

private val String.asExternalType: Type
    get() = when (this) {
        "Acceptance" -> Type.ACCEPTANCE
        "Invalidation" -> Type.INVALIDATION
        else -> throw IllegalArgumentException("type not recognized $this")
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
    get() = when (this.uppercase()) {
        GENERAL.uppercase() -> CertificateType.GENERAL
        TEST.uppercase() -> CertificateType.TEST
        VACCINATION.uppercase() -> CertificateType.VACCINATION
        RECOVERY.uppercase() -> CertificateType.RECOVERY
        else -> throw IllegalArgumentException()
    }

@VisibleForTesting
internal fun Instant.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(this.millis), UTC_ZONE_ID)
}

@VisibleForTesting
internal fun String.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.parse(this)
}

@VisibleForTesting
internal val ZonedDateTime.asExternalString: String
    get() = this.toString()

internal val DccData<out DccV1.MetaData>.type: String
    get() = when (certificate) {
        is VaccinationDccV1 -> VACCINATION
        is TestDccV1 -> TEST
        is RecoveryDccV1 -> RECOVERY
        else -> GENERAL
    }

internal fun List<DccValidationRule>.filterRelevantRules(
    validationClock: Instant,
    certificateType: String
): List<DccValidationRule> {
    return filter { rule ->
        rule.certificateType.uppercase() == GENERAL.uppercase() ||
            rule.certificateType.uppercase() == certificateType.uppercase()
    }.filter { rule ->
        rule.validFromInstant <= validationClock && rule.validToInstant >= validationClock
    }
}

internal val List<DccValidationRule.Description>.asMap: Map<String, String>
    get() {
        val map = mutableMapOf<String, String>()
        this.forEach { map[it.lang] = it.desc }
        return map
    }

internal val Map<String, String>.asList: List<DccValidationRule.Description>
    get() {
        val list = mutableListOf<DccValidationRule.Description>()
        this.entries.forEach {
            list.add(DccValidationRule.Description(lang = it.key, desc = it.value))
        }
        return list
    }

internal const val GENERAL = "General"
internal const val TEST = "Test"
internal const val VACCINATION = "Vaccination"
internal const val RECOVERY = "Recovery"
