package de.rki.coronawarnapp.contactdiary.storage

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryElement
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryElement
import de.rki.coronawarnapp.contactdiary.model.DefaultLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultPerson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDiaryRepository @Inject constructor() {
    private val fakeInstant = Instant.now()

    private val elements = MutableStateFlow<List<ContactDiaryElement>>(
        listOf(
            DefaultContactDiaryElement(
                createdAt = fakeInstant,
                people = emptyList(),
                locations = emptyList()
            )
        )
    )

    // TODO use the actual day arugment in future
    fun filterForDay(day: Instant) = elements.flatMapConcat { it.asFlow() }.filter { it.createdAt.isEqual(fakeInstant) }

    suspend fun addDummyPerson() {
        val latestElement = elements.value[0]

        val newElement = DefaultContactDiaryElement(
            createdAt = latestElement.createdAt,
            people = latestElement.people + listOf(DefaultPerson(fullName = "Someone")),
            locations = latestElement.locations
        )

        elements.emit(listOf(newElement))
    }

    suspend fun addDummyLocation() {
        val latestElement = elements.value[0]

        val newElement = DefaultContactDiaryElement(
            createdAt = latestElement.createdAt,
            people = latestElement.people,
            locations = latestElement.locations + listOf(DefaultLocation(locationName = "Somewhere"))
        )

        elements.emit(listOf(newElement))
    }
}
