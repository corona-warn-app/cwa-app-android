package de.rki.coronawarnapp.bugreporting.debuglog.upload.server

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth.LogUploadAuthApiV1
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth.LogUploadOtp
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.ElsOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.ElsOtpRequestAndroid
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject

@Reusable
class LogUploadServer @Inject constructor(
    private val uploadApiProvider: Lazy<LogUploadApi>,
    private val authApiProvider: Lazy<LogUploadAuthApiV1>,
    private val deviceAttestation: DeviceAttestation,
    private val configProvider: AppConfigProvider
) {

    private val uploadApi: LogUploadApi
        get() = uploadApiProvider.get()

    private val authApi: LogUploadAuthApiV1
        get() = authApiProvider.get()

    suspend fun getAuthorizedOTP(): LogUploadOtp {
        val otp = UUID.randomUUID()
        Timber.tag(TAG).d("getAuthorizedOTP() trying to authorize %s", otp)

        val elsOtp = ElsOtp.ELSOneTimePassword.newBuilder().apply {
            setOtp(otp.toString())
        }.build()

        val attestationRequest = object : DeviceAttestation.Request {
            override val scenarioPayload: ByteArray = elsOtp.toByteArray()
        }
        val attestionResult = deviceAttestation.attest(attestationRequest)
        Timber.tag(TAG).d("Attestation passed, requesting authorization from server for %s", attestionResult)

        val appConfig = configProvider.currentConfig.first()
        attestionResult.requirePass(appConfig.logUpload.safetyNetRequirements)

        val elsRequest = ElsOtpRequestAndroid.ELSOneTimePasswordRequestAndroid.newBuilder().apply {
            authentication = attestionResult.accessControlProtoBuf
            payload = elsOtp
        }.build()

        val authResponse = authApi.authOTP(elsRequest)

        return LogUploadOtp(
            otp = otp.toString(),
            expirationDate = authResponse.expirationDate
        )
    }

    suspend fun uploadLog(otp: LogUploadOtp, log: File) {
    }

    companion object {
        private const val TAG = "LogUploadServer"
    }
}
