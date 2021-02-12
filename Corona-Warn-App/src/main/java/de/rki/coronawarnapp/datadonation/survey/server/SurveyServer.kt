package de.rki.coronawarnapp.datadonation.survey.server

import com.google.protobuf.ByteString
import dagger.Lazy
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtpRequestAndroid
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveyServer @Inject constructor(
    private val surveyApi: Lazy<SurveyApiV1>,
    private val dispatcherProvider: DispatcherProvider
) {

    private val api: SurveyApiV1
        get() = surveyApi.get()

    suspend fun authOTP(
        data: OneTimePassword,
        deviceAttestation: DeviceAttestation.Result
    ): SurveyApiV1.DataDonationResponse = withContext(dispatcherProvider.IO) {
        Timber.d("authOTP()")

        val dataDonationPayload = EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid.newBuilder()
            .setPayload(
                EdusOtp.EDUSOneTimePassword.newBuilder()
                    .setOtp(data.uuid.toString())
                    .setOtpBytes(ByteString.copyFrom(data.payloadForRequest))
            )
            .setAuthentication(deviceAttestation.accessControlProtoBuf)
            .build()

        api.authOTP(
            requestBody = dataDonationPayload
        )
    }
}
