package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import android.os.Parcelable
import com.google.common.io.BaseEncoding
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import java.time.Instant

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
     *  @see [de.rki.coronawarnapp.presencetracing.checkins.CheckIn]
     */
    @IgnoredOnParcel
    val locationIdHash: ByteString by lazy { locationId.toTraceLocationIdHash() }

    fun isBeforeStartTime(now: Instant): Boolean = startDate?.isAfter(now) ?: false

    fun isAfterEndTime(now: Instant): Boolean = endDate?.isBefore(now) ?: false

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
