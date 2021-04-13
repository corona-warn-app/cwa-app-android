package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import androidx.annotation.StringRes
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import javax.inject.Inject

@Reusable
class TraceLocationVerifier @Inject constructor() {
    @Suppress("ReturnCount")
    fun verifyTraceLocation(protoQrCodePayload: TraceLocationOuterClass.QRCodePayload): VerificationResult {
        val traceLocation = protoQrCodePayload.traceLocation()

        if (traceLocation.description.isEmpty()) {
            return VerificationResult.Invalid.Description
        }

        if (traceLocation.description.length > QR_CODE_DESCRIPTION_MAX_LENGTH) {
            return VerificationResult.Invalid.Description
        }

        if (traceLocation.description.lines().size > 1) {
            return VerificationResult.Invalid.Description
        }

        if (traceLocation.address.isEmpty()) {
            return VerificationResult.Invalid.Address
        }

        if (traceLocation.address.length > QR_CODE_ADDRESS_MAX_LENGTH) {
            return VerificationResult.Invalid.Address
        }

        if (traceLocation.address.lines().size > 1) {
            return VerificationResult.Invalid.Address
        }

        // If both are 0 do nothing else check start is smaller than end or return error
        if (!(
            protoQrCodePayload.locationData.startTimestamp == 0L &&
                protoQrCodePayload.locationData.endTimestamp == 0L
            )
        ) {
            if (protoQrCodePayload.locationData.startTimestamp > protoQrCodePayload.locationData.endTimestamp) {
                return VerificationResult.Invalid.StartEndTime
            }
        }

        if (traceLocation.cryptographicSeed.size != CROWD_NOTIFIER_CRYPTO_SEED_LENGTH) {
            return VerificationResult.Invalid.CryptographicSeed
        }

        return VerificationResult.Valid(
            VerifiedTraceLocation(protoQrCodePayload)
        )
    }

    sealed class VerificationResult {
        data class Valid(val verifiedTraceLocation: VerifiedTraceLocation) : VerificationResult()

        sealed class Invalid(@StringRes val errorTextRes: Int) : VerificationResult() {
            object Description : Invalid(R.string.trace_location_checkins_qr_code_invalid_description)
            object Address : Invalid(R.string.trace_location_checkins_qr_code_invalid_address)
            object StartEndTime : Invalid(R.string.trace_location_checkins_qr_code_invalid_times)
            object CryptographicSeed :
                Invalid(R.string.trace_location_checkins_qr_code_invalid_cryptographic_seed)
        }
    }

    companion object {
        private const val CROWD_NOTIFIER_CRYPTO_SEED_LENGTH = 16
        private const val QR_CODE_DESCRIPTION_MAX_LENGTH = 100
        private const val QR_CODE_ADDRESS_MAX_LENGTH = 100
    }
}
