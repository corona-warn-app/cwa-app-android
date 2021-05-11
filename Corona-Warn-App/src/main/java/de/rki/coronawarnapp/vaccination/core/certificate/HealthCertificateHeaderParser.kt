package de.rki.coronawarnapp.vaccination.core.certificate

import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class HealthCertificateHeaderParser @Inject constructor() {

    fun decode(map: CBORObject): CoseCertificateHeader = try {
        var issuer: String? = null
        map[keyIssuer]?.let {
            issuer = it.AsString()
        }
        var issuedAt: Instant? = null
        map[keyIssuedAt]?.let {
            issuedAt = Instant.ofEpochSecond(it.AsNumber().ToInt64Checked())
        }
        var expiresAt: Instant? = null
        map[keyExpiresAt]?.let {
            expiresAt = Instant.ofEpochSecond(it.AsNumber().ToInt64Checked())
        }

        HealthCertificateHeader(
            issuer = issuer!!,
            issuedAt = issuedAt!!,
            expiresAt = expiresAt!!
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
