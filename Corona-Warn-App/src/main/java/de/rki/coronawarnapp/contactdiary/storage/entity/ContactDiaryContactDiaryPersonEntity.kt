package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson

@Entity
data class ContactDiaryContactDiaryPersonEntity(override var fullName: String) : ContactDiaryPerson {
    @PrimaryKey(autoGenerate = true)
    var personId: Long = 0L
}
