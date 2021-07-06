package de.rki.coronawarnapp.covidcertificate.validation.core.validation.wrapper

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.EvaluatedDccRule
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class CertLogicEngineWrapper @Inject constructor(
    private val dccJsonSchema: DccJsonSchema
) {

    private val engine: DefaultCertLogicEngine by lazy {
        DefaultCertLogicEngine(DefaultJsonLogicValidator())
    }

    fun process(
        rules: List<DccValidationRule>,
        validationClock: Instant,
        certificate: DccData<out DccV1.MetaData>,
        countryCode: String,
    ): Set<EvaluatedDccRule> = engine.validate(
        hcertVersionString = certificate.certificate.version,
        schemaJson = dccJsonSchema.rawSchema,
        rules = rules.map { it.asExternalRule },
        externalParameter = assembleExternalParameter(certificate, validationClock, countryCode),
        payload = certificate.certificateJson
    ).map { it.asEvaluatedDccRule }.toSet()
}
