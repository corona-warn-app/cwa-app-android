package de.rki.coronawarnapp.contactdiary.model

data class DefaultContactDiaryLocation(
    override val locationId: Long = 0L,
    override var locationName: String,
    override val stableId: Long = locationId
) : ContactDiaryLocation
