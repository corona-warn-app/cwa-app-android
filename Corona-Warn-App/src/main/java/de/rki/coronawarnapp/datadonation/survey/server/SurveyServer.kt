package de.rki.coronawarnapp.datadonation.survey.server

import com.google.protobuf.ByteString
import dagger.Lazy
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
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
        data: OneTimePassword
    ) = withContext(dispatcherProvider.IO) {
        Timber.d("authOTP()")

        val dataDonationPayload = EdusOtp.EDUSOneTimePassword.newBuilder()
            .setOtp(data.uuid.toString())
            .setOtpBytes(ByteString.copyFrom(data.payloadForRequest))
            .build()

        api.authOTP(
            requestBody = dataDonationPayload
        )
    }
}
