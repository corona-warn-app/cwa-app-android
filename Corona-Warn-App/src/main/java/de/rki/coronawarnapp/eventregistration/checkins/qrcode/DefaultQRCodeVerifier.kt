package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.security.SignatureValidation
import okio.ByteString.Companion.decodeBase64
import timber.log.Timber
import javax.inject.Inject

class DefaultQRCodeVerifier @Inject constructor(
    private val signatureValidation: SignatureValidation
) : QRCodeVerifier {

    override suspend fun verify(rawTraceLocation: ByteArray): QRCodeVerifyResult {
        Timber.tag(TAG).v("Verifying: %s", rawTraceLocation)

        val signedTraceLocation = try {
            TraceLocationOuterClass.SignedTraceLocation.parseFrom(rawTraceLocation)
        } catch (e: Exception) {
            throw InvalidQRCodeDataException(cause = e, message = "QR-code data could not be parsed.")
        }
        Timber.tag(TAG).d("Parsed to signed location: %s", signedTraceLocation)

        val isValid = try {
            signatureValidation.hasValidSignature(
                signedTraceLocation.location.toByteArray(),
                sequenceOf(signedTraceLocation.signature.toStringUtf8().decodeBase64()!!.toByteArray())
            )
        } catch (e: Exception) {
            throw InvalidQRCodeDataException(cause = e, message = "Verification failed.")
        }

        if (!isValid) {
            throw InvalidQRCodeSignatureException(message = "QR-code did not match signature.")
        }

        return QRCodeVerifyResult(signedTraceLocation)
    }

    companion object {
        private const val TAG = "DefaultQRCodeVerifier"
    }
}
