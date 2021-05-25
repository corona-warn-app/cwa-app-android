package de.rki.coronawarnapp.contactdiary.storage.repo

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

@Suppress("TooManyFunctions")
interface ContactDiaryRepository {

    // Location
    val locations: Flow<List<ContactDiaryLocation>>
    suspend fun addLocation(contactDiaryLocation: ContactDiaryLocation): ContactDiaryLocation
    suspend fun updateLocation(contactDiaryLocation: ContactDiaryLocation)
    suspend fun deleteLocation(contactDiaryLocation: ContactDiaryLocation)
    suspend fun deleteLocations(contactDiaryLocations: List<ContactDiaryLocation>)
    suspend fun deleteAllLocations()

    // Location visit
    val locationVisits: Flow<List<ContactDiaryLocationVisit>>
    fun locationVisitsForDate(date: LocalDate): Flow<List<ContactDiaryLocationVisit>>
    suspend fun addLocationVisit(contactDiaryLocationVisit: ContactDiaryLocationVisit)
    suspend fun updateLocationVisit(
        visitId: Long,
        update: (ContactDiaryLocationVisit) -> ContactDiaryLocationVisit
    )

    suspend fun deleteLocationVisit(contactDiaryLocationVisit: ContactDiaryLocationVisit)
    suspend fun deleteLocationVisits(contactDiaryLocationVisits: List<ContactDiaryLocationVisit>)
    suspend fun deleteAllLocationVisits()

    // Person
    val people: Flow<List<ContactDiaryPerson>>
    suspend fun addPerson(contactDiaryPerson: ContactDiaryPerson): ContactDiaryPerson
    suspend fun updatePerson(contactDiaryPerson: ContactDiaryPerson)
    suspend fun deletePerson(contactDiaryPerson: ContactDiaryPerson)
    suspend fun deletePeople(contactDiaryPeople: List<ContactDiaryPerson>)
    suspend fun deleteAllPeople()

    // Person encounter
    val personEncounters: Flow<List<ContactDiaryPersonEncounter>>
    fun personEncountersForDate(date: LocalDate): Flow<List<ContactDiaryPersonEncounter>>
    suspend fun addPersonEncounter(contactDiaryPersonEncounter: ContactDiaryPersonEncounter)
    suspend fun updatePersonEncounter(
        encounterId: Long,
        update: (ContactDiaryPersonEncounter) -> ContactDiaryPersonEncounter
    )

    suspend fun deletePersonEncounter(contactDiaryPersonEncounter: ContactDiaryPersonEncounter)
    suspend fun deletePersonEncounters(contactDiaryPersonEncounters: List<ContactDiaryPersonEncounter>)
    suspend fun deleteAllPersonEncounters()

    // Tests
    val testResults: Flow<List<ContactDiaryCoronaTestEntity>>
    suspend fun updateTests(tests: Map<CoronaTestGUID, CoronaTest>)

    // Clean
    suspend fun clear()
}
