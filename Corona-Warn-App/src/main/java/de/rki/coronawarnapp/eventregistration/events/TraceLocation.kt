package de.rki.coronawarnapp.eventregistration.events

import android.os.Parcelable
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import okio.ByteString
import org.joda.time.Instant

interface TraceLocation : Parcelable {
    val guid: String
    val version: Int
    val type: TraceLocationOuterClass.TraceLocationType
    val description: String
    val address: String
    val startDate: Instant?
    val endDate: Instant?
    val defaultCheckInLengthInMinutes: Int?
    val signature: ByteString
}
