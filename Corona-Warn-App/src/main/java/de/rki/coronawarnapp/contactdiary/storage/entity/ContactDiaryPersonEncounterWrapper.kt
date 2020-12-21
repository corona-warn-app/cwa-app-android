package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Embedded
import androidx.room.Relation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.sortByNameAndIdASC

data class ContactDiaryPersonEncounterWrapper(
    @Embedded val contactDiaryPersonEncounterEntity: ContactDiaryPersonEncounterEntity,
    @Relation(parentColumn = "fkPersonId", entityColumn = "personId")
    val contactDiaryPersonEntity: ContactDiaryPersonEntity
)

fun ContactDiaryPersonEncounterWrapper.toContactDiaryPersonEncounter(): ContactDiaryPersonEncounter =
    DefaultContactDiaryPersonEncounter(
        id = this.contactDiaryPersonEncounterEntity.id,
        date = this.contactDiaryPersonEncounterEntity.date,
        contactDiaryPerson = contactDiaryPersonEntity
    )

fun List<ContactDiaryPersonEncounterWrapper>.toContactDiaryPersonEncounterSortedList():
    List<ContactDiaryPersonEncounter> =
    this.map { it.toContactDiaryPersonEncounter() }
        .sortByNameAndIdASC()
