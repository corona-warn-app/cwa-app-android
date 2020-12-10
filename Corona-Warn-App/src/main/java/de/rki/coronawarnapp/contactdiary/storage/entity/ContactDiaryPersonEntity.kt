package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson

@Entity
data class ContactDiaryPersonEntity(
    @PrimaryKey(autoGenerate = true) override val personId: Long = 0L,
    override var fullName: String
) : ContactDiaryPerson

fun ContactDiaryPerson.toContactDiaryPersonEntity(): ContactDiaryPersonEntity =
    ContactDiaryPersonEntity(this.personId, this.fullName)
