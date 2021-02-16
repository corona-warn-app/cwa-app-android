package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.storage.OTPRepository
import de.rki.coronawarnapp.datadonation.survey.server.SurveyServer
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Surveys @Inject constructor(
    private val deviceAttestation: DeviceAttestation,
    private val appConfigProvider: AppConfigProvider,
    private val surveyServer: SurveyServer,
    private val oneTimePasswordRepo: OTPRepository,
    dispatcherProvider: DispatcherProvider,
    private val urlProvider: SurveyUrlProvider,
    private val timeStamper: TimeStamper
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
        val now = timeStamper.nowUTC

        oneTimePasswordRepo.otpAuthorizationResult?.apply {
            if (authorized && redeemedAt.toDateTime().monthOfYear() == now.toDateTime().monthOfYear()) {
                throw SurveyException(SurveyException.Type.ALREADY_PARTICIPATED_THIS_MONTH)
            }
        }

        // generate OTP
        val oneTimePassword = oneTimePasswordRepo.otp ?: oneTimePasswordRepo.generateOTP()

        // check device
        val attestationResult = deviceAttestation.attest(object : DeviceAttestation.Request {
            override val scenarioPayload: ByteArray
                get() = EdusOtp.EDUSOneTimePassword.newBuilder()
                    .setOtp(oneTimePassword.uuid.toString())
                    .build()
                    .toByteArray()
        })
        attestationResult.requirePass(config.safetyNetRequirements)

        // request validation from server
        val errorCode = surveyServer.authOTP(oneTimePassword, attestationResult).errorCode
        val result = OTPAuthorizationResult(oneTimePassword.uuid, errorCode == null, now)
        oneTimePasswordRepo.otpAuthorizationResult = result

        if (result.authorized) {
            return Survey(
                type = Type.HIGH_RISK_ENCOUNTER,
                surveyLink = urlProvider.provideUrl(type, oneTimePassword.uuid)
            )
        } else {
            throw SurveyException(SurveyException.Type.OTP_NOT_AUTHORIZED, errorCode)
        }
    }

    fun resetSurvey(type: Type) {
        if (type == Type.HIGH_RISK_ENCOUNTER) {
            Timber.d("Discarding one time password for survey about previous high-risk state.")
            oneTimePasswordRepo.clear()
        }
    }

    enum class Type {
        HIGH_RISK_ENCOUNTER
    }

    data class Survey(
        val type: Type,
        val surveyLink: String
    )
}
