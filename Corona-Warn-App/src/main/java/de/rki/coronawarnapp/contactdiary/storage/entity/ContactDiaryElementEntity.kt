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
    )
    override val people: MutableList<PersonEntity> = mutableListOf(),
    @Relation(
        parentColumn = "date",
        entityColumn = "locationId",
        associateBy = Junction(ContactDiaryElementLocationXRef::class)
    )
    override val locations: MutableList<LocationEntity> = mutableListOf()
) : ContactDiaryElement {
    override val date: LocalDate
        get() = contactDiaryDateEntity.date
}
