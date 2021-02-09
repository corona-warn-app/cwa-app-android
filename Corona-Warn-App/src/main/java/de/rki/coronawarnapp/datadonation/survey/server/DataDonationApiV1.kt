package de.rki.coronawarnapp.datadonation.survey.server

import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import retrofit2.http.Body
import retrofit2.http.POST

interface DataDonationApiV1 {

    @POST("version/v1/blabla")
    suspend fun authOTP(
        @Body requestBody: EdusOtp.EDUSOneTimePassword
    )
}
