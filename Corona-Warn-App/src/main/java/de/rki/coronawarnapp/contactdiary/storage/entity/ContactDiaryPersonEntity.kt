package de.rki.coronawarnapp.contactdiary.storage.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.util.trimToLength
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "persons")
data class ContactDiaryPersonEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "personId") override val personId: Long = 0L,
    @ColumnInfo(name = "fullName") override var fullName: String,
    @ColumnInfo(name = "phoneNumber") override val phoneNumber: String?,
    @ColumnInfo(name = "emailAddress") override val emailAddress: String?
) : ContactDiaryPerson, Parcelable {
    override val stableId: Long
        get() = personId
}

private const val MAX_CHARACTERS = 250
private fun String.trimMaxCharacters(): String = this.trimToLength(MAX_CHARACTERS)

fun ContactDiaryPerson.toContactDiaryPersonEntity(): ContactDiaryPersonEntity =
    ContactDiaryPersonEntity(
        personId = this.personId,
        fullName = this.fullName.trimMaxCharacters(),
        phoneNumber = this.phoneNumber?.trimMaxCharacters(),
        emailAddress = this.emailAddress?.trimMaxCharacters()
    )
