package de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.ElsOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.ElsOtpRequestAndroid
import kotlinx.coroutines.flow.first
import java.time.Instant
import timber.log.Timber
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject

@Reusable
class LogUploadAuthorizer @Inject constructor(
    private val authApiProvider: Lazy<LogUploadAuthApiV1>,
    private val deviceAttestation: DeviceAttestation,
    private val configProvider: AppConfigProvider
) {

    private val authApi: LogUploadAuthApiV1
        get() = authApiProvider.get()

    suspend fun getAuthorizedOTP(otp: UUID = UUID.randomUUID()): LogUploadOtp {
        Timber.tag(TAG).d("getAuthorizedOTP() trying to authorize %s", otp)

        val elsOtp = ElsOtp.ELSOneTimePassword.newBuilder().apply {
            setOtp(otp.toString())
        }.build()

        val appConfig = configProvider.currentConfig.first()

        val attestationRequest = object : DeviceAttestation.Request {
            override val configData: ConfigData = appConfig
            override val checkDeviceTime: Boolean = false
            override val scenarioPayload: ByteArray = elsOtp.toByteArray()
        }
        val attestationResult = deviceAttestation.attest(attestationRequest)
        Timber.tag(TAG).d("Attestation passed, requesting authorization from server for %s", attestationResult)

        attestationResult.requirePass(appConfig.logUpload.safetyNetRequirements)

        val elsRequest = ElsOtpRequestAndroid.ELSOneTimePasswordRequestAndroid.newBuilder().apply {
            authentication = attestationResult.accessControlProtoBuf
            payload = elsOtp
        }.build()

        val authResponse = authApi.authOTP(elsRequest).also {
            Timber.tag(TAG).v("Auth response received: %s", it)
        }

        val expirationDate = if (authResponse.expirationDate.isNotEmpty()) {
            try {
                OffsetDateTime.parse(authResponse.expirationDate).toInstant()
            } catch (e: Exception) {
                Timber.e(e, "Can't parse expirationDate: ${authResponse.expirationDate}")
                Instant.EPOCH
            }
        } else {
            Instant.EPOCH
        }

        return LogUploadOtp(otp = otp.toString(), expirationDate = expirationDate).also {
            Timber.tag(TAG).d("%s created", it)
        }
    }

    companion object {
        private const val TAG = "LogUploadOtpServer"
    }
}
