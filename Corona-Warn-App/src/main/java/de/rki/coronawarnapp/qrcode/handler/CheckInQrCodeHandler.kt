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

    fun handleQrCode(qrcode: CheckInQrCode): Result {
        val payload = qrcode.qrCodePayload
        val location = payload.traceLocation()
        val locationData = payload.locationData
        return when {
            location.description.isEmpty() ||
                location.description.length > DESCRIPTION_MAX_LENGTH ||
                location.description.lines().size > 1 -> Result.Invalid.Description

            location.address.isEmpty() ||
                location.address.length > ADDRESS_MAX_LENGTH ||
                location.address.lines().size > 1 -> Result.Invalid.Address

            // If both are 0 do nothing else check start is smaller than end or return error
            !(locationData.startTimestamp == 0L && locationData.endTimestamp == 0L) &&
                locationData.startTimestamp > locationData.endTimestamp -> Result.Invalid.StartEndTime

            location.cryptographicSeed.size != CRYPTO_SEED_LENGTH -> Result.Invalid.CryptographicSeed

            else -> Result.Valid(VerifiedTraceLocation(payload))
        }
    }

    sealed class Result {
        data class Valid(val verifiedTraceLocation: VerifiedTraceLocation) : Result()

        sealed class Invalid(@StringRes val errorTextRes: Int) : Result() {
            object Description : Invalid(R.string.trace_location_checkins_qr_code_invalid_description)
            object Address : Invalid(R.string.trace_location_checkins_qr_code_invalid_address)
            object StartEndTime : Invalid(R.string.trace_location_checkins_qr_code_invalid_times)
            object CryptographicSeed : Invalid(R.string.trace_location_checkins_qr_code_invalid_cryptographic_seed)
        }
    }

    companion object {
        private const val CRYPTO_SEED_LENGTH = 16
        private const val DESCRIPTION_MAX_LENGTH = 100
        private const val ADDRESS_MAX_LENGTH = 100
    }
}
