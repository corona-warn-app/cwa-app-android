package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import androidx.annotation.StringRes
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import javax.inject.Inject

@Reusable
class TraceLocationVerifier @Inject constructor() {
    fun verifyTraceLocation(protoQrCodePayload: TraceLocationOuterClass.QRCodePayload): VerificationResult {
        val traceLocation = protoQrCodePayload.traceLocation()

        if (traceLocation.description.isEmpty()) {
            return VerificationResult.Invalid.EmptyDescription
        }

        if (traceLocation.address.isEmpty()) {
            return VerificationResult.Invalid.EmptyAddress
        }

        if (traceLocation.cryptographicSeed.size != CROWD_NOTIFIER_CRYPTO_SEED_LENGTH) {
            return VerificationResult.Invalid.InvalidCryptographicSeed
        }

        return VerificationResult.Valid(
            VerifiedTraceLocation(protoQrCodePayload)
        )
    }

    sealed class VerificationResult {
        data class Valid(val verifiedTraceLocation: VerifiedTraceLocation) : VerificationResult()

        sealed class Invalid(@StringRes val errorTextRes: Int) : VerificationResult() {
            object EmptyDescription : Invalid(R.string.trace_location_checkins_qr_code_invalid_description)
            object EmptyAddress : Invalid(R.string.trace_location_checkins_qr_code_invalid_address)
            object InvalidCryptographicSeed :
                Invalid(R.string.trace_location_checkins_qr_code_invalid_cryptographic_seed)
        }
    }

    companion object {
        const val CROWD_NOTIFIER_CRYPTO_SEED_LENGTH = 16
    }
}
