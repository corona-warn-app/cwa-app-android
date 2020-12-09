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
                people = listOf(DefaultPerson(fullName = "Test Person")),
                locations = listOf(DefaultLocation(locationName = "Test Location"))
            )
        )
    )

    // TODO use the actual day arugment in future
    fun filterForDay(day: Instant) = elements.flatMapConcat { it.asFlow() }.filter { it.createdAt.isEqual(fakeInstant) }

    suspend fun deleteLocation() {
        val latestElement = elements.value[0]

        val newElement = DefaultContactDiaryElement(
            createdAt = latestElement.createdAt,
            people = latestElement.people,
            locations = listOf()
        )

        elements.emit(listOf(newElement))
    }
}
