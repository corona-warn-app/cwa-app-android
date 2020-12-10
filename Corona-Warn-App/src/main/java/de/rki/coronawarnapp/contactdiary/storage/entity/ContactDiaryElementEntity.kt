package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryElement
import org.joda.time.LocalDate

data class ContactDiaryElementEntity(
    @Embedded val contactDiaryDateEntity: ContactDiaryDateEntity,
    @Relation(
        parentColumn = "date",
        entityColumn = "personId",
        associateBy = Junction(ContactDiaryElementPersonXRef::class)
    ) override val contactDiaryPeople: MutableList<ContactDiaryPersonEntity> = mutableListOf(),
    @Relation(
        parentColumn = "date",
        entityColumn = "locationId",
        associateBy = Junction(ContactDiaryElementLocationXRef::class)
    ) override val contactDiaryLocations: MutableList<ContactDiaryLocationEntity> = mutableListOf()
) : ContactDiaryElement {
    override val date: LocalDate
        get() = contactDiaryDateEntity.date
}

fun ContactDiaryElement.toContactDiaryElementEntity(): ContactDiaryElementEntity = when (this) {
    is ContactDiaryElementEntity -> this
    else -> ContactDiaryElementEntity(
        contactDiaryDateEntity = this.date.toContactDiaryDateEntity(),
        contactDiaryLocations = this.contactDiaryLocations
            .map { it.toContactDiaryLocationEntity() }
            .toMutableList(),
        contactDiaryPeople = this.contactDiaryPeople
            .map { it.toContactDiaryPersonEntity() }
            .toMutableList()
    )
}
