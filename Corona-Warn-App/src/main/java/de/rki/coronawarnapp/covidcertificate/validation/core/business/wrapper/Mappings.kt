package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import dgca.verifier.app.engine.Result
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.RuleCertificateType
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
        issuerCountryCode = certificate.header.issuer,
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
        ruleCertificateType = certificateType.asExternalCertificateType,
        descriptions = description.map { it.languageCode to it.description }.toMap(),
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
    typeDcc = type.asDccType,
    version = version,
    schemaVersion = schemaVersion,
    engine = engine,
    engineVersion = engineVersion,
    certificateType = ruleCertificateType.asInternalString,
    description = descriptions.map { DccValidationRule.Description(description = it.key, languageCode = it.value) },
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

private val DccValidationRule.Type.asExternalType: Type
    get() = when (this) {
        DccValidationRule.Type.ACCEPTANCE -> Type.ACCEPTANCE
        DccValidationRule.Type.INVALIDATION -> Type.INVALIDATION
    }

private val RuleCertificateType.asInternalString: String
    get() = when (this) {
        RuleCertificateType.GENERAL -> GENERAL
        RuleCertificateType.TEST -> TEST
        RuleCertificateType.VACCINATION -> VACCINATION
        RuleCertificateType.RECOVERY -> RECOVERY
    }

private val String.asExternalCertificateType: RuleCertificateType
    get() = when (this.uppercase()) {
        GENERAL.uppercase() -> RuleCertificateType.GENERAL
        TEST.uppercase() -> RuleCertificateType.TEST
        VACCINATION.uppercase() -> RuleCertificateType.VACCINATION
        RECOVERY.uppercase() -> RuleCertificateType.RECOVERY
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

internal val DccData<out DccV1.MetaData>.asExternalType: CertificateType
    get() = when (certificate) {
        is VaccinationDccV1 -> CertificateType.VACCINATION
        is TestDccV1 -> CertificateType.TEST
        is RecoveryDccV1 -> CertificateType.RECOVERY
        else -> throw IllegalArgumentException("Unknown certificate type.")
    }

internal val DccData<out DccV1.MetaData>.typeString: String
    get() = when (certificate) {
        is VaccinationDccV1 -> VACCINATION
        is TestDccV1 -> TEST
        is RecoveryDccV1 -> RECOVERY
        else -> throw IllegalArgumentException("Unknown certificate type.")
    }

internal fun List<DccValidationRule>.filterRelevantRules(
    validationClock: Instant,
    certificateType: String,
    country: DccCountry,
): List<DccValidationRule> = this
    .asSequence()
    .filter { it.country.uppercase() == country.countryCode.uppercase() }
    .filter { rule ->
        rule.certificateType.uppercase() == GENERAL.uppercase() ||
            rule.certificateType.uppercase() == certificateType.uppercase()
    }
    .filter { rule ->
        rule.validFromInstant <= validationClock && rule.validToInstant >= validationClock
    }
    .groupBy { it.identifier }
    .mapNotNull { entry ->
        entry.value.maxByOrNull { it.versionSemVer }
    }
    .toList()

internal const val GENERAL = "General"
internal const val TEST = "Test"
internal const val VACCINATION = "Vaccination"
internal const val RECOVERY = "Recovery"
