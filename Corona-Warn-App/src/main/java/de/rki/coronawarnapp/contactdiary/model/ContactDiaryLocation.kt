package de.rki.coronawarnapp.contactdiary.model

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import de.rki.coronawarnapp.util.lists.HasStableId
import java.util.Locale

interface ContactDiaryLocation : HasStableId {
    val locationId: Long
    var locationName: String
    val phoneNumber: String?
    val emailAddress: String?
    val traceLocationID: TraceLocationId?
}

fun List<ContactDiaryLocation>.sortByNameAndIdASC(): List<ContactDiaryLocation> =
    this.sortedWith(compareBy({ it.locationName.lowercase()}, { it.locationId }))
