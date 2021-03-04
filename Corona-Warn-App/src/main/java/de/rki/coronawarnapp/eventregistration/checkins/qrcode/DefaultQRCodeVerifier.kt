package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.VerificationKeys
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class DefaultQRCodeVerifier @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val verificationKeys: VerificationKeys,
) : QRCodeVerifier {
    override suspend fun verify(encodedEvent: String) =
        withContext(dispatcherProvider.Default) {
            try {
                Timber.i("encodedEvent:$encodedEvent")
                val signedEvent = SignedEventOuterClass.SignedEvent
                    .parseFrom(encodedEvent.decodeBase32().toByteArray())

                Timber.i("signedEvent:$signedEvent")
                verificationKeys.hasInvalidSignature(
                    signedEvent.event.toByteArray(),
                    signedEvent.signature.toByteArray()
                ).also {
                    Timber.i("Qr-code event verification passed:$it")
                }
            } catch (e: Exception) {
                Timber.d(e, "Qr-code event verification failed")
                false
            }
        }
}
