package de.rki.coronawarnapp.contactdiary.storage.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "persons")
data class ContactDiaryPersonEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "personId") override val personId: Long = 0L,
    @ColumnInfo(name = "fullName") override var fullName: String
) : ContactDiaryPerson, Parcelable {
    override val stableId: Long
        get() = personId
}

fun ContactDiaryPerson.toContactDiaryPersonEntity(): ContactDiaryPersonEntity =
    ContactDiaryPersonEntity(this.personId, this.fullName)
