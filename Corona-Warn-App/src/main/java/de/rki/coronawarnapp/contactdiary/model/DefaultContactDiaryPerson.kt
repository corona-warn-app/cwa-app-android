package de.rki.coronawarnapp.contactdiary.model

data class DefaultContactDiaryPerson(
    override val personId: Long = 0L,
    override var fullName: String,
    override val stableId: Long = personId
) : ContactDiaryPerson
