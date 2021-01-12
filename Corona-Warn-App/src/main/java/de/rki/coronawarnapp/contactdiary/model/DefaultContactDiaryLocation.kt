package de.rki.coronawarnapp.contactdiary.model

data class DefaultContactDiaryLocation(
    override val locationId: Long = 0L,
    override var locationName: String
) : ContactDiaryLocation {
    override val stableId: Long
        get() = locationId
}
