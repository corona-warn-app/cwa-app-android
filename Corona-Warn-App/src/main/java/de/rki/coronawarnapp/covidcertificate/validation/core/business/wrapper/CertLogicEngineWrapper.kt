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
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dgca.verifier.app.engine.DefaultAffectedFieldsDataRetriever
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import kotlinx.coroutines.flow.first
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CertLogicEngineWrapper @Inject constructor(
    private val valueSetWrapper: ValueSetWrapper,
    private val dccJsonSchema: DccJsonSchema,
    @BaseJackson private val objectMapper: ObjectMapper,
) {

    private val engine: DefaultCertLogicEngine by lazy {
        DefaultCertLogicEngine(
            DefaultAffectedFieldsDataRetriever(
                schemaJsonNode = objectMapper.readTree(dccJsonSchema.rawSchema),
                objectMapper = objectMapper
            ),
            DefaultJsonLogicValidator()
        )
    }

    suspend fun process(
        rules: List<DccValidationRule>,
        validationDate: LocalDate,
        validationTime: LocalTime,
        certificate: DccData<out DccV1.MetaData>,
        countryCode: String,
    ): Set<EvaluatedDccRule> {

        if (rules.isEmpty()) {
            Timber.i("No rules to be validated. Abort.")
            return emptySet()
        }

        val valueMap = when (certificate.certificate) {
            is VaccinationDccV1 -> valueSetWrapper.valueSetVaccination.first()
            is TestDccV1 -> valueSetWrapper.valueSetTest.first()
            is RecoveryDccV1 -> valueSetWrapper.valueSetRecovery.first()
            else -> emptyMap()
        }
        val externalParameter = assembleExternalParameter(
            certificate,
            validationDate,
            validationTime,
            countryCode,
            valueMap
        )

        Timber.i("Rules to be validated are:")
        rules.forEach {
            Timber.i("Rule ${it.identifier} ${it.version}.")
        }

        return engine.validate(
            hcertVersionString = certificate.certificate.version,
            rules = rules.map { it.asExternalRule },
            externalParameter = externalParameter,
            payload = certificate.certificateJson,
            certificateType = certificate.asExternalType
        ).map {
            it.asEvaluatedDccRule
        }.toSet().also {
            Timber.i("Evaluated rules are:")
            it.forEach {
                Timber.i("Rule ${it.rule.identifier} ${it.rule.version} has resulted in ${it.result}.")
            }
        }
    }
}
