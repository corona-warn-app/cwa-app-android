package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson

@Entity
data class ContactDiaryPersonEntity(override var fullName: String) : ContactDiaryPerson {
    @PrimaryKey(autoGenerate = true)
    var personId: Long = 0L
}

fun ContactDiaryPerson.toContactDiaryPersonEntity(personId: Long = 0L): ContactDiaryPersonEntity = when (this) {
    is ContactDiaryPersonEntity -> this
    else -> ContactDiaryPersonEntity(this.fullName)
        .apply { this.personId = personId }
}
