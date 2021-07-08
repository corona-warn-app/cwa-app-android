package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class CertLogicEngineWrapper @Inject constructor() {

    private val engine: DefaultCertLogicEngine by lazy {
        DefaultCertLogicEngine(DefaultJsonLogicValidator())
    }

    fun process(
        rules: List<DccValidationRule>,
        validationClock: Instant,
        certificate: DccData<out DccV1.MetaData>,
        countryCode: String,
        schemaJson: String
    ): Set<EvaluatedDccRule> = engine.validate(
        hcertVersionString = certificate.certificate.version,
        schemaJson = schemaJson,
        rules = rules.map { it.asExternalRule },
        externalParameter = assembleExternalParameter(certificate, validationClock, countryCode),
        payload = certificate.certificateJson
    ).map { it.asEvaluatedDccRule }.toSet()
}
