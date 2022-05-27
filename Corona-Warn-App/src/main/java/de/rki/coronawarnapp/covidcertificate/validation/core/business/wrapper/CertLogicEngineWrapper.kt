package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import dgca.verifier.app.engine.DefaultCertLogicEngine
import kotlinx.coroutines.flow.first
import java.time.OffsetDateTime
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CertLogicEngineWrapper @Inject constructor(
    private val valueSetWrapper: ValueSetWrapper,
    private val engine: Lazy<DefaultCertLogicEngine>
) {

    suspend fun process(
        rules: List<DccValidationRule>,
        validationDateTime: OffsetDateTime,
        certificate: DccData<out DccV1.MetaData>,
        countryCode: String,
    ): Set<EvaluatedDccRule> {

        if (rules.isEmpty()) {
            Timber.tag(TAG).w("No rules to be validated. Abort.")
            return emptySet()
        }

        val externalParameter = assembleExternalParameter(
            certificate,
            validationDateTime,
            countryCode,
            valueSetWrapper.valueMap.first()
        )

        Timber.tag(TAG).i("Rules to be validated are:")
        rules.forEach {
            Timber.tag(TAG).i("Rule ${it.identifier} ${it.version}.")
        }

        return engine.get()
            .validate(
                hcertVersionString = certificate.certificate.version,
                rules = rules.map { it.asExternalRule },
                externalParameter = externalParameter,
                payload = certificate.certificateJson,
                certificateType = certificate.asExternalType
            )
            .map { result ->
                result.validationErrors?.forEach {
                    Timber.tag(TAG).e(it, "Errors during validation of %s", result.rule.identifier)
                }
                result.asEvaluatedDccRule
            }
            .also { evaluated ->
                Timber.tag(TAG).i("Evaluated rules are:")
                evaluated.forEach {
                    Timber.tag(TAG).i("Rule ${it.rule.identifier} ${it.rule.version} has resulted in ${it.result}.")
                }
            }
            .toSet()
    }

    companion object {
        private const val TAG = "CertLogicEngineWrapper"
    }
}
