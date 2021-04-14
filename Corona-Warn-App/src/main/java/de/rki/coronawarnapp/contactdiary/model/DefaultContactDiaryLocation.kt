package de.rki.coronawarnapp.contactdiary.model

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId

data class DefaultContactDiaryLocation(
    override val locationId: Long = 0L,
    override var locationName: String,
    override val phoneNumber: String? = null,
    override val emailAddress: String? = null,
    override val traceLocationID: TraceLocationId? = null
) : ContactDiaryLocation {
    override val stableId: Long
        get() = locationId
}
