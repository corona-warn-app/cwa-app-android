package de.rki.coronawarnapp.contactdiary.storage.repo

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryElement
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

interface ContactDiaryRepository {

    //Flows
    val contactDiaryDates: Flow<List<LocalDate>>
    val contactDiaryElements: Flow<List<ContactDiaryElement>>
    val locations: Flow<List<ContactDiaryLocation>>
    val people: Flow<List<ContactDiaryPerson>>

    //Date
    suspend fun addDate(date: LocalDate)
    suspend fun addDates(dates: List<LocalDate>)
    suspend fun deleteDate(date: LocalDate)
    suspend fun deleteAllDates()

    //ContactDiaryElement
    suspend fun addPersonToDate(contactDiaryPerson: ContactDiaryPerson, date: LocalDate)
    suspend fun addPeopleToDate(contactDiaryPeople: List<ContactDiaryPerson>, date: LocalDate)
    suspend fun addLocationToDate(contactDiaryLocation: ContactDiaryLocation, date: LocalDate)
    suspend fun addLocationsToDate(contactDiaryLocations: List<ContactDiaryLocation>, date: LocalDate)
    suspend fun removePersonFromDate(contactDiaryPerson: ContactDiaryPerson, date: LocalDate)
    suspend fun removePeopleFromDate(contactDiaryPeople: List<ContactDiaryPerson>, date: LocalDate)
    suspend fun removeLocationFromDate(contactDiaryLocation: ContactDiaryLocation, date: LocalDate)
    suspend fun removeLocationsFromDate(contactDiaryLocations: List<ContactDiaryLocation>, date: LocalDate)

    //Location
    suspend fun addLocation(contactDiaryLocation: ContactDiaryLocation)
    suspend fun updateLocation(contactDiaryLocation: ContactDiaryLocation)
    suspend fun deleteLocation(contactDiaryLocation: ContactDiaryLocation)
    suspend fun deleteLocations(contactDiaryLocations: List<ContactDiaryLocation>)
    suspend fun deleteAllLocations()

    //Person
    suspend fun addPerson(contactDiaryPerson: ContactDiaryPerson)
    suspend fun updatePerson(contactDiaryPerson: ContactDiaryPerson)
    suspend fun deletePerson(contactDiaryPerson: ContactDiaryPerson)
    suspend fun deletePeople(contactDiaryPeople: List<ContactDiaryPerson>)
    suspend fun deleteAllPeople()
}
