package de.rki.coronawarnapp.contactdiary.model

data class DefaultContactDiaryPerson(
    override val personId: Long = 0L,
    override var fullName: String
) : ContactDiaryPerson {
    override val stableId: Long
        get() = personId
}
