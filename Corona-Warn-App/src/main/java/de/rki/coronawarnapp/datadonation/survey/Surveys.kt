package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.storage.OTPRepository
import de.rki.coronawarnapp.datadonation.survey.server.SurveyServer
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    suspend fun isConsentNeeded(type: Type): ConsentResult {

        when (type) {
            Type.HIGH_RISK_ENCOUNTER -> {

                // If no OTP was ever authorized, we need a consent.
                val authResult: OTPAuthorizationResult = oneTimePasswordRepo.getOtpAuthorizationResult().first()
                    ?: return ConsentResult.Needed

                // If we already have an authorized OTP for this high-risk state
                // we can skip the consent and directly show the url in the browser.
                // We know that the otp belongs to the current high-risk state, because the authResult gets
                // invalidated on high to low risk state transitions.
                if (authResult.authorized && !authResult.invalidated) {
                    val surveyLink = urlProvider.provideUrl(type, authResult.uuid)
                    return ConsentResult.AlreadyGiven(surveyLink)
                }

                // Finally, we need a consent for stored OTPs where the authorization failed (authorized == false)
                // or when the app shows a new high-risk card (and therefore authResult was previously invalidated when the
                // risk changed from high to low)
                return ConsentResult.Needed
            }
        }
    }

    sealed class ConsentResult {
        object Needed : ConsentResult()
        data class AlreadyGiven(val surveyLink: String) : ConsentResult()
    }

    suspend fun requestDetails(type: Type): Survey {
        val config = appConfigProvider.getAppConfig().survey
        Timber.v("Requested survey: %s", config)
        val now = timeStamper.nowUTC

        oneTimePasswordRepo.getOtpAuthorizationResult().first()?.apply {
            if (authorized &&
                redeemedAt.toLocalDateUtc().month == now.toLocalDateUtc().month &&
                redeemedAt.toLocalDateUtc().year == now.toLocalDateUtc().year
            ) {
                throw SurveyException(SurveyException.Type.ALREADY_PARTICIPATED_THIS_MONTH)
            }
        }

        // generate OTP
        val oneTimePassword = oneTimePasswordRepo.getOtp() ?: oneTimePasswordRepo.generateOTP()

        // check device
        val attestationResult = deviceAttestation.attest(
            object : DeviceAttestation.Request {
                override val scenarioPayload: ByteArray
                    get() = oneTimePassword.payloadForRequest
            }
        )
        attestationResult.requirePass(config.safetyNetRequirements)

        // request validation from server
        val errorCode = surveyServer.authOTP(oneTimePassword, attestationResult).errorCode
        val result = OTPAuthorizationResult(
            uuid = oneTimePassword.uuid,
            authorized = errorCode == null,
            redeemedAt = now
        )
        oneTimePasswordRepo.updateOtpAuthorizationResult(result)

        if (result.authorized) {
            return Survey(
                type = Type.HIGH_RISK_ENCOUNTER,
                surveyLink = urlProvider.provideUrl(type, oneTimePassword.uuid)
            )
        } else {
            throw SurveyException(SurveyException.Type.OTP_NOT_AUTHORIZED, errorCode)
        }
    }

    suspend fun resetSurvey(type: Type) {
        val authResult = oneTimePasswordRepo.getOtpAuthorizationResult().first()
        if (type == Type.HIGH_RISK_ENCOUNTER && authResult != null) {
            Timber.d("Invalidating one time password for survey about previous high-risk state.")
            oneTimePasswordRepo.updateOtpAuthorizationResult(authResult.toInvalidatedInstance())
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
