package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import dgca.verifier.app.engine.DefaultAffectedFieldsDataRetriever
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import kotlinx.coroutines.flow.first
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class CertLogicEngineWrapper @Inject constructor(
    private val valueSetWrapper: ValueSetWrapper,
    private val dccJsonSchema: DccJsonSchema,
) {

    private val engine: DefaultCertLogicEngine by lazy {
        DefaultCertLogicEngine(
            DefaultAffectedFieldsDataRetriever(
                schemaJsonNode = ObjectMapper().readTree(dccJsonSchema.rawSchema),
                objectMapper = ObjectMapper()
            ),
            DefaultJsonLogicValidator()
        )
    }

    suspend fun process(
        rules: List<DccValidationRule>,
        validationClock: Instant,
        certificate: DccData<out DccV1.MetaData>,
        countryCode: String,
    ): Set<EvaluatedDccRule> {

        val valueMap = when (certificate.certificate) {
            is VaccinationDccV1 -> valueSetWrapper.valueSetVaccination.first()
            is TestDccV1 -> valueSetWrapper.valueSetTest.first()
            is RecoveryDccV1 -> valueSetWrapper.valueSetRecovery.first()
            else -> emptyMap()
        }
        val externalParameter = assembleExternalParameter(
            certificate,
            validationClock,
            countryCode,
            valueMap
        )
        return engine.validate(
            hcertVersionString = certificate.certificate.version,
            rules = rules.map { it.asExternalRule },
            externalParameter = externalParameter,
            payload = certificate.certificateJson,
            certificateType = certificate.asExternalType
        ).map { it.asEvaluatedDccRule }.toSet()
    }
}
