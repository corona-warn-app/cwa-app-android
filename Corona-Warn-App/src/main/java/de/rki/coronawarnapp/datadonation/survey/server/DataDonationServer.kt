package de.rki.coronawarnapp.datadonation.survey.server

import com.google.protobuf.ByteString
import dagger.Lazy
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataDonationServer @Inject constructor(
    private val dataDonationApi: Lazy<DataDonationApiV1>
) {

    private val api: DataDonationApiV1
        get() = dataDonationApi.get()

    suspend fun authOTP(
        data: OneTimePassword
    ) = withContext(Dispatchers.IO) {
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
