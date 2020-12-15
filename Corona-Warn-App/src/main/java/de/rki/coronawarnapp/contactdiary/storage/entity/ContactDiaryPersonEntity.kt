package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson

@Entity(tableName = "persons")
data class ContactDiaryPersonEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "personId") override val personId: Long = 0L,
    @ColumnInfo(name = "fullName") override var fullName: String
) : ContactDiaryPerson

fun ContactDiaryPerson.toContactDiaryPersonEntity(): ContactDiaryPersonEntity =
    ContactDiaryPersonEntity(this.personId, this.fullName)
