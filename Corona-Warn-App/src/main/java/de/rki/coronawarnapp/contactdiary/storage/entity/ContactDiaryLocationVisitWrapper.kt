package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Embedded
import androidx.room.Relation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.sortByNameAndIdASC

class ContactDiaryLocationVisitWrapper(
    @Embedded val contactDiaryLocationVisitEntity: ContactDiaryLocationVisitEntity,
    @Relation(parentColumn = "fkLocationId", entityColumn = "locationId")
    val contactDiaryLocationEntity: ContactDiaryLocationEntity
)

fun ContactDiaryLocationVisitWrapper.toContactDiaryLocationVisit(): ContactDiaryLocationVisit =
    DefaultContactDiaryLocationVisit(
        id = this.contactDiaryLocationVisitEntity.id,
        date = this.contactDiaryLocationVisitEntity.date,
        contactDiaryLocation = this.contactDiaryLocationEntity
    )

fun List<ContactDiaryLocationVisitWrapper>.toContactDiaryLocationVisitSortedList(): List<ContactDiaryLocationVisit> =
    this.map { it.toContactDiaryLocationVisit() }
        .sortByNameAndIdASC()
