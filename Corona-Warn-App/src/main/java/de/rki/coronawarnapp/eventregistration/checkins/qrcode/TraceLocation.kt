package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import android.os.Parcelable
import com.google.common.io.BaseEncoding
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.joda.time.Duration
import org.joda.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@Parcelize
data class TraceLocation(
    val id: Long = 0L,
    val type: TraceLocationOuterClass.TraceLocationType,
    val description: String,
    val address: String,
    val startDate: Instant?,
    val endDate: Instant?,
    val defaultCheckInLengthInMinutes: Int?,
    val cryptographicSeed: ByteString,
    val cnPublicKey: String,
    val version: Int = VERSION,
) : Parcelable {

    /**
     * Return a url for [TraceLocation] to be used as an input for [QrCodeGenerator]
     * URL format https://e.coronawarn.app?v=1#QR_CODE_PAYLOAD_BASE64URL
     */
    @IgnoredOnParcel
    val locationUrl: String by lazy {
        val payloadBytes = qrCodePayload().toByteArray()
        val base64Url = BaseEncoding.base64Url().omitPadding().encode(payloadBytes)
        AUTHORITY.plus(base64Url)
    }

    /**
     *  Returns a byte sequence that serves as an identifier for the trace location.
     *  The ID is the byte representation of SHA-256 hash.
     */
    @IgnoredOnParcel
    val locationId: TraceLocationId by lazy {
        val cwaDomain = CWA_GUID.toByteArray()
        val payloadBytes = qrCodePayload().toByteArray()
        val totalByteSequence = cwaDomain + payloadBytes
        totalByteSequence.toByteString().sha256()
    }

    /**
     *  Returns SHA-256 hash of [locationId] which itself may also be SHA-256 hash.
     *  For privacy reasons TraceTimeIntervalWarnings only offer a hash of the actual locationId.
     *
     *  @see [de.rki.coronawarnapp.eventregistration.checkins.CheckIn]
     */
    @IgnoredOnParcel
    val locationIdHash: ByteString by lazy { locationId.toTraceLocationIdHash() }

    fun isBeforeStartTime(now: Instant): Boolean = startDate?.isAfter(now) ?: false

    fun isAfterEndTime(now: Instant): Boolean = endDate?.isBefore(now) ?: false

    /**
     * Evaluates the default auto-checkout length depending on the current time
     */
    @Suppress("ReturnCount")
    fun getDefaultAutoCheckoutLengthInMinutes(now: Instant): Int {

        // min valid value is 00:15h
        val minDefaultAutoCheckOutLengthInMinutes = 15

        // max valid value is 23:45h
        val maxDefaultAutoCheckOutLengthInMinutes = (TimeUnit.HOURS.toMinutes(23) + 45).toInt()

        // for temporary traceLocations, a defaultCheckInLength is always available
        if (defaultCheckInLengthInMinutes != null) {

            if (defaultCheckInLengthInMinutes < 15) {
                return minDefaultAutoCheckOutLengthInMinutes
            }

            if (defaultCheckInLengthInMinutes > maxDefaultAutoCheckOutLengthInMinutes) {
                return maxDefaultAutoCheckOutLengthInMinutes
            }

            return roundToNearest15Minutes(defaultCheckInLengthInMinutes)
        } else {

            if (endDate == null) {
                return minDefaultAutoCheckOutLengthInMinutes
            }

            if (now.isAfter(endDate)) {
                return minDefaultAutoCheckOutLengthInMinutes
            }

            val minutesUntilEndDate = Duration(now, endDate).standardMinutes.toInt()

            if (minutesUntilEndDate < minDefaultAutoCheckOutLengthInMinutes) {
                return minDefaultAutoCheckOutLengthInMinutes
            }

            if (minutesUntilEndDate > maxDefaultAutoCheckOutLengthInMinutes) {
                return maxDefaultAutoCheckOutLengthInMinutes
            }

            return roundToNearest15Minutes(minutesUntilEndDate)
        }
    }

    private fun roundToNearest15Minutes(minutes: Int): Int {
        val roundingStepInMinutes = 15
        return Duration
            .standardMinutes(
                (minutes.toFloat() / roundingStepInMinutes)
                    .roundToLong() * roundingStepInMinutes
            )
            .standardMinutes.toInt()
    }

    companion object {
        /**
         * Trace location version. This is a static data and not calculated from [TraceLocation]
         */
        const val VERSION = 1

        private const val AUTHORITY = "https://e.coronawarn.app?v=$VERSION#"
        private const val CWA_GUID = "CWA-GUID"
    }
}

typealias TraceLocationId = ByteString

fun TraceLocationId.toTraceLocationIdHash() = sha256()

fun List<TraceLocationEntity>.toTraceLocations() = this.map { it.toTraceLocation() }

fun TraceLocationEntity.toTraceLocation() = TraceLocation(
    id = id,
    type = type,
    description = description,
    address = address,
    startDate = startDate,
    endDate = endDate,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    cryptographicSeed = cryptographicSeedBase64.decodeBase64()!!,
    cnPublicKey = cnPublicKey,
    version = version
)
