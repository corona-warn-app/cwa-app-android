package de.rki.coronawarnapp.contactdiary.storage

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitWrapper
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterWrapper
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTest

@RunWith(AndroidJUnit4::class)
class ContactDiaryDatabaseTest : BaseTest() {

    // TestData
    private val date = LocalDate.now()
    private val person = ContactDiaryPersonEntity(
        personId = 1,
        fullName = "Peter",
        emailAddress = "person-emailAddress",
        phoneNumber = "person-phoneNumber"
    )
    private val location = ContactDiaryLocationEntity(
        locationId = 2,
        locationName = "Rewe Wiesloch",
        emailAddress = "location-emailAddress",
        phoneNumber = "location-phoneNumber"
    )
    private val personEncounter = ContactDiaryPersonEncounterEntity(
        id = 3,
        date = date,
        fkPersonId = person.personId,
        durationClassification = ContactDiaryPersonEncounter.DurationClassification.MORE_THAN_15_MINUTES,
        withMask = true,
        wasOutside = false,
        circumstances = "You could see the smile under his mask."
    )
    private val locationVisit = ContactDiaryLocationVisitEntity(
        id = 4,
        date = date,
        fkLocationId = location.locationId,
        duration = Duration.standardMinutes(99).millis,
        circumstances = "I had to buy snacks."
    )

    // DB
    private val contactDiaryDatabase: ContactDiaryDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        ContactDiaryDatabase::class.java
    ).build()

    private val personDao = contactDiaryDatabase.personDao()
    private val locationDao = contactDiaryDatabase.locationDao()
    private val personEncounterDao = contactDiaryDatabase.personEncounterDao()
    private val locationVisitDao = contactDiaryDatabase.locationVisitDao()

    private fun List<ContactDiaryPersonEncounterWrapper>.toContactDiaryPersonEncounterEntityList(): List<ContactDiaryPersonEncounterEntity> =
        this.map { it.contactDiaryPersonEncounterEntity }

    private fun List<ContactDiaryLocationVisitWrapper>.toContactDiaryLocationVisitEntityList(): List<ContactDiaryLocationVisitEntity> =
        this.map { it.contactDiaryLocationVisitEntity }

    @After
    fun teardown() {
        contactDiaryDatabase.clearAllTables()
    }

    @Test
    fun checkPersonEncounterDeletedWhenReferencedPersonDeleted() = runBlocking {
        val personFlow = personDao.allEntries()
        val personEncounterFlow = personEncounterDao
            .allEntries()
            .map { it.toContactDiaryPersonEncounterEntityList() }

        personFlow.first() shouldBe emptyList()
        personEncounterFlow.first() shouldBe emptyList()

        personDao.insert(person)
        personEncounterDao.insert(personEncounter)
        personFlow.first() shouldBe listOf(person)
        personEncounterFlow.first() shouldBe listOf(personEncounter)

        personDao.delete(person)
        personFlow.first() shouldBe emptyList()
        personEncounterFlow.first() shouldBe emptyList()
    }

    @Test
    fun checkLocationVisitDeletedWhenReferencedLocationDeleted() = runBlocking {
        val locationFlow = locationDao.allEntries()
        val locationVisitFlow = locationVisitDao
            .allEntries()
            .map { it.toContactDiaryLocationVisitEntityList() }

        locationFlow.first() shouldBe emptyList()
        locationVisitFlow.first() shouldBe emptyList()

        locationDao.insert(location)
        locationVisitDao.insert(locationVisit)
        locationFlow.first() shouldBe listOf(location)
        locationVisitFlow.first() shouldBe listOf(locationVisit)

        locationDao.delete(location)
        locationFlow.first() shouldBe emptyList()
        locationVisitFlow.first() shouldBe emptyList()
    }

    @Test
    fun getCorrectEntityForDate() = runBlocking {
        val yesterday = LocalDate.now().minusDays(1)
        val tomorrow = LocalDate.now().plusDays(1)
        val personEncounterYesterday = ContactDiaryPersonEncounterEntity(
            id = 5,
            date = yesterday,
            fkPersonId = person.personId,
            durationClassification = ContactDiaryPersonEncounter.DurationClassification.LESS_THAN_15_MINUTES,
            withMask = false,
            wasOutside = false,
            circumstances = "encounter-yesterday"
        )
        val personEncounterTomorrow = ContactDiaryPersonEncounterEntity(
            id = 6,
            date = tomorrow,
            fkPersonId = person.personId,
            durationClassification = ContactDiaryPersonEncounter.DurationClassification.MORE_THAN_15_MINUTES,
            withMask = true,
            wasOutside = true,
            circumstances = "encounter-today"
        )
        val locationVisitYesterday = ContactDiaryLocationVisitEntity(
            id = 7,
            date = yesterday,
            fkLocationId = location.locationId,
            duration = Duration.standardMinutes(42).millis,
            circumstances = "visit-yesterday"
        )
        val locationVisitTomorrow = ContactDiaryLocationVisitEntity(
            id = 8,
            date = tomorrow,
            fkLocationId = location.locationId,
            duration = Duration.standardMinutes(1).millis,
            circumstances = "visit-today"
        )
        val encounterList = listOf(personEncounter, personEncounterYesterday, personEncounterTomorrow)
        val visitList = listOf(locationVisit, locationVisitYesterday, locationVisitTomorrow)
        val personEncounterFlow = personEncounterDao.allEntries().map { it.toContactDiaryPersonEncounterEntityList() }
        val locationVisitFlow = locationVisitDao.allEntries()
            .map { it.toContactDiaryLocationVisitEntityList() }

        personDao.insert(person)
        personEncounterDao.insert(encounterList)
        locationDao.insert(location)
        locationVisitDao.insert(visitList)

        personEncounterFlow.first() shouldBe encounterList
        locationVisitFlow.first() shouldBe visitList

        personEncounterDao.entitiesForDate(yesterday).first().toContactDiaryPersonEncounterEntityList() shouldBe listOf(
            personEncounterYesterday
        )
        personEncounterDao.entitiesForDate(date).first().toContactDiaryPersonEncounterEntityList() shouldBe listOf(
            personEncounter
        )
        personEncounterDao.entitiesForDate(tomorrow).first().toContactDiaryPersonEncounterEntityList() shouldBe listOf(
            personEncounterTomorrow
        )

        locationVisitDao.entitiesForDate(yesterday).first().toContactDiaryLocationVisitEntityList() shouldBe listOf(
            locationVisitYesterday
        )
        locationVisitDao.entitiesForDate(date).first().toContactDiaryLocationVisitEntityList() shouldBe listOf(
            locationVisit
        )
        locationVisitDao.entitiesForDate(tomorrow).first().toContactDiaryLocationVisitEntityList() shouldBe listOf(
            locationVisitTomorrow
        )
    }

    @Test
    fun updatingLocationVisits(): Unit = runBlocking {
        val locationVisitFlow = locationVisitDao.allEntries().map { it.toContactDiaryLocationVisitEntityList() }

        locationDao.insert(location)
        locationVisitDao.insert(listOf(locationVisit))

        locationVisitFlow.first().single() shouldBe locationVisit

        val updatedLocation = locationVisit.copy(
            duration = 123L,
            circumstances = "Suspicious"
        )
        locationVisitDao.update(updatedLocation)

        locationVisitFlow.first().single() shouldBe updatedLocation
    }

    @Test
    fun updatingPersonEncounters(): Unit = runBlocking {
        val personEncounterFlow = personEncounterDao.allEntries().map { it.toContactDiaryPersonEncounterEntityList() }

        personDao.insert(person)
        personEncounterDao.insert(personEncounter)

        personEncounterFlow.first().single() shouldBe personEncounter

        val updatedEncounter = personEncounter.copy(
            withMask = true,
            wasOutside = false,
            durationClassification = ContactDiaryPersonEncounter.DurationClassification.MORE_THAN_15_MINUTES,
            circumstances = "He lend me a coffee cup but the handle broke and it dropped onto my laptop."
        )
        personEncounterDao.update(updatedEncounter)
        personEncounterFlow.first().single() shouldBe updatedEncounter
    }
}
