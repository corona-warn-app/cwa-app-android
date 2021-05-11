package de.rki.coronawarnapp.vaccination.core.server.proof

import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.ByteString
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface VaccinationProofApiV2 {

    // Returns COSE representation (as byte sequence) of a Proof Certificate
    @Headers("Content-Type: application/cbor")
    @POST("/api/certify/v2/reissue/cbor")
    suspend fun obtainProofCertificate(@Body cose: RequestBody): ResponseBody

    // Returns string as for the QR Code of a Proof Certificate (starting with HC1: )
    @Headers(
        "Content-Type: application/cbor",
        "Accept: application/cbor+base45"
    )
    @POST("/api/certify/v2/reissue/cbor")
    suspend fun obtainProofCertificateBase45(@Body cose: ByteString): String
}
