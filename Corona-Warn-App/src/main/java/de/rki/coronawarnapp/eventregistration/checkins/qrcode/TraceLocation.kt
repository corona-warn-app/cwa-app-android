package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import android.os.Parcelable
import de.rki.coronawarnapp.eventregistration.events.TraceLocationUserInput
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import kotlinx.parcelize.Parcelize
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import java.security.SecureRandom

const val TRACE_LOCATION_VERSION = 1

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
    val cnPublicKey: String = "hardcoded public key TODO: replace with real one",
    val version: Int = TRACE_LOCATION_VERSION,
) : Parcelable {

    fun isBeforeStartTime(now: Instant): Boolean = startDate?.isAfter(now) ?: false

    fun isAfterEndTime(now: Instant): Boolean = endDate?.isBefore(now) ?: false
}

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

fun TraceLocationUserInput.toTraceLocation(secureRandom: SecureRandom) = TraceLocation(
    type = type,
    description = description,
    address = address,
    startDate = startDate,
    endDate = endDate,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    // cryptographic seed is a sequence of 16 random bytes
    cryptographicSeed = ByteArray(16).apply { secureRandom.nextBytes(this) }.toByteString()
)
