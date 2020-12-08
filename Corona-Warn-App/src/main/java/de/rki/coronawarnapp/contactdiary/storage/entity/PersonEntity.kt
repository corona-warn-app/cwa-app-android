package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import de.rki.coronawarnapp.contactdiary.model.Person

@Entity
data class PersonEntity(override var fullName: String) : Person
