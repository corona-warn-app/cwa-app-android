package de.rki.coronawarnapp.contactdiary.storage.repo

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryElement
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

@file:Suppress("TooManyFunctions")
interface ContactDiaryRepository {

    // Flows
    val contactDiaryDates: Flow<LocalDate>
    val contactDiaryElements: Flow<ContactDiaryElement>
    val locations: Flow<ContactDiaryLocation>
    val people: Flow<ContactDiaryPerson>

    // Date
    suspend fun addDate(date: LocalDate)
    suspend fun addDates(dates: List<LocalDate>)
    suspend fun deleteDate(date: LocalDate)
    suspend fun deleteAllDates()

    // ContactDiaryElement
    suspend fun addContactDiaryElement(contactDiaryElement: ContactDiaryElement)
    suspend fun addPersonToDate(contactDiaryPerson: ContactDiaryPerson, date: LocalDate)
    suspend fun addPeopleToDate(contactDiaryPeople: List<ContactDiaryPerson>)
    suspend fun addLocationToDate(contactDiaryLocation: ContactDiaryLocation, date: LocalDate)
    suspend fun addLocationsToDate(contactDiaryLocations: List<ContactDiaryLocation>, date: LocalDate)
    suspend fun removePersonFromDate(contactDiaryPerson: ContactDiaryPerson, date: LocalDate)
    suspend fun removePeopleFromDate(contactDiaryPeople: List<ContactDiaryPerson>)
    suspend fun removeLocationFromDate(contactDiaryLocation: ContactDiaryLocation, date: LocalDate)
    suspend fun removeLocationsFromDate(contactDiaryLocations: List<ContactDiaryLocation>, date: LocalDate)
    suspend fun removeContactDiaryElement(contactDiaryElement: ContactDiaryElement)

    // Location
    suspend fun addLocation(contactDiaryLocation: ContactDiaryLocation)
    suspend fun updateLocation(contactDiaryLocation: ContactDiaryLocation)
    suspend fun deleteLocation(contactDiaryLocation: ContactDiaryLocation)
    suspend fun deleteLocations(contactDiaryLocations: List<ContactDiaryLocation>)
    suspend fun deleteAllLocations()

    // Person
    suspend fun addPerson(contactDiaryPerson: ContactDiaryPerson)
    suspend fun updatePerson(contactDiaryPerson: ContactDiaryPerson)
    suspend fun deletePerson(contactDiaryPerson: ContactDiaryPerson)
    suspend fun deletePeople(contactDiaryPeople: List<ContactDiaryPerson>)
    suspend fun deleteAllPeople()
}
