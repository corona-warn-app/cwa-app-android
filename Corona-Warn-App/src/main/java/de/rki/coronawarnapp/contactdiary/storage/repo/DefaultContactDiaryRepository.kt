package de.rki.coronawarnapp.contactdiary.storage.repo

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryElement
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryDateDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryElementDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@file:Suppress("TooManyFunctions")
class DefaultContactDiaryRepository @Inject constructor(
    contactDiaryDateDao: ContactDiaryDateDao,
    contactDiaryElementDao: ContactDiaryElementDao,
    contactDiaryLocationDao: ContactDiaryLocationDao,
    contactDiaryPersonDao: ContactDiaryPersonDao
) : ContactDiaryRepository {

    override val contactDiaryDates: Flow<LocalDate> = contactDiaryDateDao
        .allEntries()
        .map { it.date }

    override val contactDiaryElements: Flow<ContactDiaryElement> = contactDiaryElementDao.allEntries()
    override val locations: Flow<ContactDiaryLocation> = contactDiaryLocationDao.allEntries()
    override val people: Flow<ContactDiaryPerson> = contactDiaryPersonDao.allEntries()

    // Date
    override suspend fun addDate(date: LocalDate) {
        TODO("Not yet implemented")
    }

    override suspend fun addDates(dates: List<LocalDate>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDate(date: LocalDate) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllDates() {
        TODO("Not yet implemented")
    }

    // ContactDiaryElement
    override suspend fun addContactDiaryElement(contactDiaryElement: ContactDiaryElement) {
        TODO("Not yet implemented")
    }

    override suspend fun addPersonToDate(contactDiaryPerson: ContactDiaryPerson, date: LocalDate) {
        TODO("Not yet implemented")
    }

    override suspend fun addPeopleToDate(contactDiaryPeople: List<ContactDiaryPerson>) {
        TODO("Not yet implemented")
    }

    override suspend fun addLocationToDate(contactDiaryLocation: ContactDiaryLocation, date: LocalDate) {
        TODO("Not yet implemented")
    }

    override suspend fun addLocationsToDate(contactDiaryLocations: List<ContactDiaryLocation>, date: LocalDate) {
        TODO("Not yet implemented")
    }

    override suspend fun removePersonFromDate(contactDiaryPerson: ContactDiaryPerson, date: LocalDate) {
        TODO("Not yet implemented")
    }

    override suspend fun removePeopleFromDate(contactDiaryPeople: List<ContactDiaryPerson>) {
        TODO("Not yet implemented")
    }

    override suspend fun removeLocationFromDate(contactDiaryLocation: ContactDiaryLocation, date: LocalDate) {
        TODO("Not yet implemented")
    }

    override suspend fun removeLocationsFromDate(contactDiaryLocations: List<ContactDiaryLocation>, date: LocalDate) {
        TODO("Not yet implemented")
    }

    override suspend fun removeContactDiaryElement(contactDiaryElement: ContactDiaryElement) {
        TODO("Not yet implemented")
    }

    // Location
    override suspend fun addLocation(contactDiaryLocation: ContactDiaryLocation) {
        TODO("Not yet implemented")
    }

    override suspend fun updateLocation(contactDiaryLocation: ContactDiaryLocation) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLocation(contactDiaryLocation: ContactDiaryLocation) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLocations(contactDiaryLocations: List<ContactDiaryLocation>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllLocations() {
        TODO("Not yet implemented")
    }

    // Person
    override suspend fun addPerson(contactDiaryPerson: ContactDiaryPerson) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePerson(contactDiaryPerson: ContactDiaryPerson) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePerson(contactDiaryPerson: ContactDiaryPerson) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePeople(contactDiaryPeople: List<ContactDiaryPerson>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllPeople() {
        TODO("Not yet implemented")
    }
}
