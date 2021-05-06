package de.rki.coronawarnapp.vaccination.core.server.proof

import okio.ByteString
import retrofit2.http.Body
import retrofit2.http.Headers

import retrofit2.http.POST

interface VaccinationProofApiV2 {

    @Headers("Content-Type: application/cbor")
    @POST("/api/certify/v2/reissue/cbor")
    suspend fun obtainProofCertificate(@Body cose: ByteString): ByteString
}
