package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.storage.OTPRepository
import de.rki.coronawarnapp.datadonation.survey.server.SurveyServer
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Surveys @Inject constructor(
    private val deviceAttestation: DeviceAttestation,
    private val settings: SurveySettings,
    private val appConfigProvider: AppConfigProvider,
    private val surveyServer: SurveyServer,
    private val otpRepo: OTPRepository,
    dispatcherProvider: DispatcherProvider,
    private val urlProvider: SurveyUrlProvider
) {

    val availableSurveys: Flow<Collection<Type>> by lazy {
        appConfigProvider.currentConfig
            .flowOn(dispatcherProvider.Default)
            .map {
                mutableListOf<Type>().apply {
                    if (it.survey.surveyOnHighRiskEnabled) {
                        add(Type.HIGH_RISK_ENCOUNTER)
                    }
                }
            }
    }

    suspend fun requestDetails(type: Type): Survey {
        val surveyConfig = appConfigProvider.getAppConfig().survey
        Timber.v("Requested survey: %s", surveyConfig)
        if (type == Type.HIGH_RISK_ENCOUNTER && !surveyConfig.surveyOnHighRiskEnabled) {
            // FIXME
            throw IllegalArgumentException("not enabled")
        }
        otpRepo.otpAuthorizationResult?.apply {
            if (authorized && redeemedAt.toDateTime().monthOfYear() == Instant.now().toDateTime().monthOfYear()) {
                // FIXME
                throw IllegalArgumentException("already did this year")
            }
        }
        val otp = otpRepo.otp ?: otpRepo.generateOTP()
        val errorCode = surveyServer.authOTP(
            otp,
            deviceAttestation.attest(object : DeviceAttestation.Request {
                override val scenarioPayload: ByteArray
                    get() = otp.payloadForRequest
            })
        ).errorCode

        val result = OTPAuthorizationResult(otp.uuid, errorCode == null, Instant.now())
        otpRepo.otpAuthorizationResult = result

        if (result.authorized) {
            return Survey(
                type = Type.HIGH_RISK_ENCOUNTER,
                surveyLink = urlProvider.provideUrl(type, otp.uuid)
            )
        } else {
            //FIXME
            throw IllegalArgumentException(errorCode)
        }
    }

    suspend fun resetSurvey(type: Type) {
        // TODO
    }

    enum class Type {
        HIGH_RISK_ENCOUNTER
    }

    data class Survey(
        val type: Type,
        val surveyLink: String
    )
}
