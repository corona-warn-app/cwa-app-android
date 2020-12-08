package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryElement
import de.rki.coronawarnapp.contactdiary.model.Location
import de.rki.coronawarnapp.contactdiary.model.Person
import java.time.Instant

@Entity
data class ContactDiaryElementEntity(
    override val createdAt: Instant,
    override val people: List<Person>,
    override val locations: List<Location>
) : ContactDiaryElement
