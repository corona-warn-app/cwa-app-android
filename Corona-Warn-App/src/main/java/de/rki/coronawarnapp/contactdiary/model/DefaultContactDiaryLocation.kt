package de.rki.coronawarnapp.contactdiary.model

data class DefaultContactDiaryLocation(
    override val locationId: Long = 0L,
    override var locationName: String,
    override val phoneNumber: String? = null,
    override val emailAddress: String? = null
) : ContactDiaryLocation {
    override val stableId: Long
        get() = locationId
}
