package de.rki.coronawarnapp.contactdiary.storage

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTest

@RunWith(AndroidJUnit4::class)
class ContactDiaryDatabaseTest : BaseTest() {

    // TestData
    private val date = LocalDate.now()
    private val person = ContactDiaryPersonEntity(personId = 1, fullName = "Peter")
    private val location = ContactDiaryLocationEntity(locationId = 2, locationName = "Rewe Wiesloch")
    private val personEncounter = ContactDiaryPersonEncounterEntity(id = 3, date = date, fkPersonId = person.personId)
    private val locationVisit = ContactDiaryLocationVisitEntity(id = 4, date = date, fkLocationId = location.locationId)

    // DB
    private val contactDiaryDatabase: ContactDiaryDatabase =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ContactDiaryDatabase::class.java
        )
            .build()

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
        val personEncounterYesterday =
            ContactDiaryPersonEncounterEntity(id = 5, date = yesterday, fkPersonId = person.personId)
        val personEncounterTomorrow =
            ContactDiaryPersonEncounterEntity(id = 6, date = tomorrow, fkPersonId = person.personId)
        val locationVisitYesterday =
            ContactDiaryLocationVisitEntity(id = 7, date = yesterday, fkLocationId = location.locationId)
        val locationVisitTomorrow =
            ContactDiaryLocationVisitEntity(id = 8, date = tomorrow, fkLocationId = location.locationId)
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

        personEncounterDao.entitiesForDate(yesterday).first().toContactDiaryPersonEncounterEntityList() shouldBe listOf(personEncounterYesterday)
        personEncounterDao.entitiesForDate(date).first().toContactDiaryPersonEncounterEntityList() shouldBe listOf(personEncounter)
        personEncounterDao.entitiesForDate(tomorrow).first().toContactDiaryPersonEncounterEntityList() shouldBe listOf(personEncounterTomorrow)

        locationVisitDao.entitiesForDate(yesterday).first().toContactDiaryLocationVisitEntityList() shouldBe listOf(locationVisitYesterday)
        locationVisitDao.entitiesForDate(date).first().toContactDiaryLocationVisitEntityList() shouldBe listOf(locationVisit)
        locationVisitDao.entitiesForDate(tomorrow).first().toContactDiaryLocationVisitEntityList() shouldBe listOf(locationVisitTomorrow)
    }
}
