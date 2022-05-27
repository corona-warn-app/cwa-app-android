package de.rki.coronawarnapp.covidcertificate.common.decoder

import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccHeader
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_EXP
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_ISS
import java.time.Instant
import javax.inject.Inject

@Reusable
class DccHeaderParser @Inject constructor() {

    fun parse(map: CBORObject): DccHeader = try {
        val issuer: String = map[keyIssuer]?.AsString() ?: throw InvalidHealthCertificateException(HC_CWT_NO_ISS)

        val issuedAt: Instant = map[keyIssuedAt]?.run {
            Instant.ofEpochSecond(AsNumber().ToInt64Checked())
        } ?: throw InvalidHealthCertificateException(HC_CWT_NO_ISS)

        val expiresAt: Instant = map[keyExpiresAt]?.run {
            Instant.ofEpochSecond(AsNumber().ToInt64Checked())
        } ?: throw InvalidHealthCertificateException(HC_CWT_NO_EXP)

        DccHeader(
            issuer = issuer,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
        )
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
    }

    companion object {
        private val keyIssuer = CBORObject.FromObject(1)
        private val keyExpiresAt = CBORObject.FromObject(4)
        private val keyIssuedAt = CBORObject.FromObject(6)
    }
}
