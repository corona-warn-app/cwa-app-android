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
        val config = appConfigProvider.getAppConfig().survey
        Timber.v("Requested survey: %s", config)
        /* no check here:
         * if surveyOnHighRisk is not enabled, this use case shouldn't have been started in the first place
         */
//        if (type == Type.HIGH_RISK_ENCOUNTER && !surveyConfig.surveyOnHighRiskEnabled) {
//            throw SurveyException(SurveyException.Type.HIGH_RISK_NOT_ENABLED)
//        }
        otpRepo.otpAuthorizationResult?.apply {
            if (authorized && redeemedAt.toDateTime().monthOfYear() == Instant.now().toDateTime().monthOfYear()) {
                throw SurveyException(SurveyException.Type.ALREADY_PARTICIPATED_THIS_MONTH)
            }
        }

        // generate OTP
        val otp = otpRepo.otp ?: otpRepo.generateOTP()

        // check device
        val attestationResult = deviceAttestation.attest(object : DeviceAttestation.Request {
            override val scenarioPayload: ByteArray
                get() = otp.payloadForRequest
        })
        attestationResult.requirePass(config.safetyNetRequirements)

        // request validation from server
        val errorCode = surveyServer.authOTP(otp, attestationResult).errorCode
        val result = OTPAuthorizationResult(otp.uuid, errorCode == null, Instant.now())
        otpRepo.otpAuthorizationResult = result

        if (result.authorized) {
            return Survey(
                type = Type.HIGH_RISK_ENCOUNTER,
                surveyLink = urlProvider.provideUrl(type, otp.uuid)
            )
        } else {
            throw SurveyException(SurveyException.Type.OTP_NOT_AUTHORIZED, errorCode)
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
