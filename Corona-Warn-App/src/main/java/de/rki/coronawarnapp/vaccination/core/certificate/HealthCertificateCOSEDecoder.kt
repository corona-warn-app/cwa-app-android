package de.rki.coronawarnapp.vaccination.core.certificate

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_COSE_TAG_INVALID
import timber.log.Timber
import javax.inject.Inject

class HealthCertificateCOSEDecoder @Inject constructor() {

    fun decode(input: RawCOSEObject): CBORObject = try {
        val messageObject = CBORObject.DecodeFromBytes(input.asByteArray).validate()
        val content = messageObject[2].GetByteString()
        CBORObject.DecodeFromBytes(content)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
    }

    private fun CBORObject.validate(): CBORObject {
        if (size() != 4) {
            throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
        }
        if (!HasTag(18)) {
            throw InvalidHealthCertificateException(HC_COSE_TAG_INVALID)
        }
        return this
    }
}
