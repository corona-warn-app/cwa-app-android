package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.security.SignatureValidation
import timber.log.Timber
import javax.inject.Inject

class DefaultQRCodeVerifier @Inject constructor(
    private val signatureValidation: SignatureValidation,
    private val timeStamper: TimeStamper
) : QRCodeVerifier {

    override suspend fun verify(encodedEvent: String): QRCodeVerifyResult {
        Timber.tag(TAG).v("Verifying: %s", encodedEvent)

        val signedEvent = try {
            SignedEventOuterClass.SignedEvent.parseFrom(encodedEvent.decodeBase32().toByteArray())
        } catch (e: Exception) {
            throw InvalidQRCodeDataException(cause = e, message = "QR-code data could not be parsed.")
        }
        Timber.tag(TAG).d("Parsed to signed event: %s", signedEvent)

        val isValid = try {
            signatureValidation.hasValidSignature(
                signedEvent.event.toByteArray(),
                sequenceOf(signedEvent.signature.toByteArray())
            )
        } catch (e: Exception) {
            throw InvalidQRCodeDataException(cause = e, message = "Verification failed.")
        }

        if (!isValid) {
            throw InvalidQRCodeSignatureException(message = "QR-code did not match signature.")
        }

        val now = timeStamper.nowUTC.seconds
        val start = signedEvent.event.start
        val end = signedEvent.event.end

        return when {
            // Event is not started yet
            start != 0 && start > now -> QRCodeVerifyResult.StartTimeWarning(signedEvent.event)
            // Event is already over
            end != 0 && end < now -> QRCodeVerifyResult.EndTimeWarning(signedEvent.event)
            // All looks fine
            else -> QRCodeVerifyResult.Success(signedEvent.event)
        }
    }

    companion object {
        private const val TAG = "DefaultQRCodeVerifier"
    }
}
