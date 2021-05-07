package de.rki.coronawarnapp.vaccination.decoder

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.HC_COSE_TAG_INVALID
import timber.log.Timber
import javax.inject.Inject

class COSEDecoder @Inject constructor() {
    fun decode(input: ByteArray): CBORObject {
        return try {
            val messageObject = CBORObject.DecodeFromBytes(input).validate()
            val content = messageObject[2].GetByteString()
            CBORObject.DecodeFromBytes(content)
        } catch (e: InvalidVaccinationQRCodeException) {
            throw e
        } catch (e: Throwable) {
            Timber.e(e)
            throw InvalidVaccinationQRCodeException(HC_COSE_MESSAGE_INVALID)
        }
    }

    // If there is no tag or the tag does not have the value 18 (CBOR tag value for a COSE Single Signer Data Object),
    // the operation is aborted with error code HC_COSE_TAG_INVALID.
    // If the message does not have 4 elements, the operation is aborted with error code HC_COSE_MESSAGE_INVALID. .
    private fun CBORObject.validate(): CBORObject {
        if (this.size() != 4) {
            throw InvalidVaccinationQRCodeException(HC_COSE_MESSAGE_INVALID)
        }
        if (!this.HasTag(18)) {
            throw InvalidVaccinationQRCodeException(HC_COSE_TAG_INVALID)
        }
        return this
    }
}
