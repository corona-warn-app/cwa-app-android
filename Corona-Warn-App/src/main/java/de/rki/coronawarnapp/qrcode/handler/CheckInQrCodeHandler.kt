package de.rki.coronawarnapp.qrcode.handler

import androidx.annotation.StringRes
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCode
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.traceLocation
import javax.inject.Inject

@Reusable
class CheckInQrCodeHandler @Inject constructor() {

    @Suppress("ReturnCount")
    fun handleQrCode(qrcode: CheckInQrCode): Result {
        val payload = qrcode.qrCodePayload
        val traceLocation = payload.traceLocation()

        if (traceLocation.description.isEmpty()) return Result.Invalid.Description
        if (traceLocation.description.length > QR_CODE_DESCRIPTION_MAX_LENGTH) return Result.Invalid.Description
        if (traceLocation.description.lines().size > 1) return Result.Invalid.Description
        if (traceLocation.address.isEmpty()) return Result.Invalid.Address
        if (traceLocation.address.length > QR_CODE_ADDRESS_MAX_LENGTH) return Result.Invalid.Address
        if (traceLocation.address.lines().size > 1) return Result.Invalid.Address

        // If both are 0 do nothing else check start is smaller than end or return error
        val locationData = payload.locationData
        if (!(locationData.startTimestamp == 0L && locationData.endTimestamp == 0L) &&
            locationData.startTimestamp > locationData.endTimestamp
        ) {
            return Result.Invalid.StartEndTime
        }

        if (traceLocation.cryptographicSeed.size != CROWD_NOTIFIER_CRYPTO_SEED_LENGTH) {
            return Result.Invalid.CryptographicSeed
        }

        return Result.Valid(VerifiedTraceLocation(payload))
    }

    sealed class Result {
        data class Valid(val verifiedTraceLocation: VerifiedTraceLocation) : Result()

        sealed class Invalid(@StringRes val errorTextRes: Int) : Result() {
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
