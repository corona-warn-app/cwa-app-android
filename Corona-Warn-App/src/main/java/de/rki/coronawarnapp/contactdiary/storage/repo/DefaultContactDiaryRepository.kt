package de.rki.coronawarnapp.contactdiary.storage.repo

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.sortByNameAndIdASC
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryDatabase
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationVisitDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonEncounterDao
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryLocationVisitEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryLocationVisitSortedList
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryPersonEncounterEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryPersonEncounterSortedList
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryPersonEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("TooManyFunctions")
class DefaultContactDiaryRepository @Inject constructor(
    contactDiaryDatabaseFactory: ContactDiaryDatabase.Factory
) : ContactDiaryRepository {

    private val contactDiaryDatabase: ContactDiaryDatabase by lazy {
        contactDiaryDatabaseFactory.create()
    }
    private val contactDiaryLocationDao: ContactDiaryLocationDao by lazy {
        contactDiaryDatabase.locationDao()
    }
    private val contactDiaryLocationVisitDao: ContactDiaryLocationVisitDao by lazy {
        contactDiaryDatabase.locationVisitDao()
    }
    private val contactDiaryPersonDao: ContactDiaryPersonDao by lazy {
        contactDiaryDatabase.personDao()
    }
    private val contactDiaryPersonEncounterDao: ContactDiaryPersonEncounterDao by lazy {
        contactDiaryDatabase.personEncounterDao()
    }

    // Location
    override val locations: Flow<List<ContactDiaryLocation>> by lazy {
        contactDiaryLocationDao
            .allEntries()
            .map { it.sortByNameAndIdASC() }
    }

    override suspend fun addLocation(contactDiaryLocation: ContactDiaryLocation): ContactDiaryLocation {
        Timber.d("Adding location $contactDiaryLocation")
        val id = contactDiaryLocationDao.insert(contactDiaryLocation.toContactDiaryLocationEntity())
        return contactDiaryLocationDao.entityForId(id)
    }

    override suspend fun updateLocation(contactDiaryLocation: ContactDiaryLocation) {
        Timber.d("Updating location $contactDiaryLocation")
        val contactDiaryContactDiaryLocationEntity = contactDiaryLocation.toContactDiaryLocationEntity()
        executeWhenIdNotDefault(contactDiaryContactDiaryLocationEntity.locationId) {
            contactDiaryLocationDao.update(contactDiaryContactDiaryLocationEntity)
        }
    }

    override suspend fun deleteLocation(contactDiaryLocation: ContactDiaryLocation) {
        Timber.d("Deleting location $contactDiaryLocation")
        val contactDiaryContactDiaryLocationEntity = contactDiaryLocation.toContactDiaryLocationEntity()
        executeWhenIdNotDefault(contactDiaryContactDiaryLocationEntity.locationId) {
            contactDiaryLocationDao.delete(contactDiaryContactDiaryLocationEntity)
        }
    }

    override suspend fun deleteLocations(contactDiaryLocations: List<ContactDiaryLocation>) {
        Timber.d("Deleting location $contactDiaryLocations")
        val contactDiaryLocationEntities = contactDiaryLocations
            .map {
                val contactDiaryLocationEntity = it.toContactDiaryLocationEntity()
                executeWhenIdNotDefault(contactDiaryLocationEntity.locationId)
                return@map contactDiaryLocationEntity
            }
        contactDiaryLocationDao.delete(contactDiaryLocationEntities)
    }

    override suspend fun deleteAllLocations() {
        Timber.d("Clearing contact diary location table")
        contactDiaryLocationDao.deleteAll()
    }

    // Location visit
    override val locationVisits: Flow<List<ContactDiaryLocationVisit>> by lazy {
        contactDiaryLocationVisitDao
            .allEntries()
            .map { it.toContactDiaryLocationVisitSortedList() }
    }

    override fun locationVisitsForDate(date: LocalDate): Flow<List<ContactDiaryLocationVisit>> =
        contactDiaryLocationVisitDao
            .entitiesForDate(date)
            .map { it.toContactDiaryLocationVisitSortedList() }

    override suspend fun addLocationVisit(contactDiaryLocationVisit: ContactDiaryLocationVisit) {
        Timber.d("Adding location visit $contactDiaryLocationVisit")
        val contactDiaryLocationVisitEntity = contactDiaryLocationVisit.toContactDiaryLocationVisitEntity()
        contactDiaryLocationVisitDao.insert(contactDiaryLocationVisitEntity)
    }

    override suspend fun deleteLocationVisit(contactDiaryLocationVisit: ContactDiaryLocationVisit) {
        Timber.d("Deleting location visit $contactDiaryLocationVisit")
        executeWhenIdNotDefault(contactDiaryLocationVisit.id) {
            val contactDiaryLocationVisitEntity = contactDiaryLocationVisit.toContactDiaryLocationVisitEntity()
            contactDiaryLocationVisitDao.delete(contactDiaryLocationVisitEntity)
        }
    }

    override suspend fun deleteLocationVisits(contactDiaryLocationVisits: List<ContactDiaryLocationVisit>) {
        Timber.d("Deleting location visits $contactDiaryLocationVisits")
        val contactDiaryLocationVisitsEntities = contactDiaryLocationVisits
            .map {
                val contactDiaryLocationVisitsEntity = it.toContactDiaryLocationVisitEntity()
                executeWhenIdNotDefault(contactDiaryLocationVisitsEntity.id)
                return@map contactDiaryLocationVisitsEntity
            }
        contactDiaryLocationVisitDao.delete(contactDiaryLocationVisitsEntities)
    }

    override suspend fun deleteAllLocationVisits() {
        Timber.d("Clearing contact diary location visit table")
        contactDiaryLocationVisitDao.deleteAll()
    }

    // Person
    override val people: Flow<List<ContactDiaryPerson>> by lazy {
        contactDiaryPersonDao
            .allEntries()
            .map { it.sortByNameAndIdASC() }
    }

    override suspend fun addPerson(contactDiaryPerson: ContactDiaryPerson): ContactDiaryPerson {
        Timber.d("Adding person $contactDiaryPerson")
        val id = contactDiaryPersonDao.insert(contactDiaryPerson.toContactDiaryPersonEntity())
        return contactDiaryPersonDao.entityForId(id)
    }

    override suspend fun updatePerson(contactDiaryPerson: ContactDiaryPerson) {
        Timber.d("Updating person $contactDiaryPerson")
        val contactDiaryPersonEntity = contactDiaryPerson.toContactDiaryPersonEntity()
        executeWhenIdNotDefault(contactDiaryPersonEntity.personId) {
            contactDiaryPersonDao.update(contactDiaryPersonEntity)
        }
    }

    override suspend fun deletePerson(contactDiaryPerson: ContactDiaryPerson) {
        Timber.d("Deleting person $contactDiaryPerson")
        val contactDiaryPersonEntity = contactDiaryPerson.toContactDiaryPersonEntity()
        executeWhenIdNotDefault(contactDiaryPersonEntity.personId) {
            contactDiaryPersonDao.delete(contactDiaryPersonEntity)
        }
    }

    override suspend fun deletePeople(contactDiaryPeople: List<ContactDiaryPerson>) {
        Timber.d("Deleting people $contactDiaryPeople")
        val contactDiaryPersonEntities = contactDiaryPeople
            .map {
                val contactDiaryPersonEntity = it.toContactDiaryPersonEntity()
                executeWhenIdNotDefault(contactDiaryPersonEntity.personId)
                return@map contactDiaryPersonEntity
            }
        contactDiaryPersonDao.delete(contactDiaryPersonEntities)
    }

    override suspend fun deleteAllPeople() {
        Timber.d("Clearing contact diary person table")
        contactDiaryPersonDao.deleteAll()
    }

    // Person encounter
    override val personEncounters: Flow<List<ContactDiaryPersonEncounter>> by lazy {
        contactDiaryPersonEncounterDao
            .allEntries()
            .map { it.toContactDiaryPersonEncounterSortedList() }
    }

    override fun personEncountersForDate(date: LocalDate): Flow<List<ContactDiaryPersonEncounter>> =
        contactDiaryPersonEncounterDao
            .entitiesForDate(date)
            .map { it.toContactDiaryPersonEncounterSortedList() }

    override suspend fun addPersonEncounter(contactDiaryPersonEncounter: ContactDiaryPersonEncounter) {
        Timber.d("Adding person encounter $contactDiaryPersonEncounter")
        val contactDiaryPersonEncounterEntity = contactDiaryPersonEncounter.toContactDiaryPersonEncounterEntity()
        contactDiaryPersonEncounterDao.insert(contactDiaryPersonEncounterEntity)
    }

    override suspend fun deletePersonEncounter(contactDiaryPersonEncounter: ContactDiaryPersonEncounter) {
        Timber.d("Deleting person encounter $contactDiaryPersonEncounter")
        executeWhenIdNotDefault(contactDiaryPersonEncounter.id) {
            val contactDiaryPersonEncounterEntity = contactDiaryPersonEncounter.toContactDiaryPersonEncounterEntity()
            contactDiaryPersonEncounterDao.delete(contactDiaryPersonEncounterEntity)
        }
    }

    override suspend fun deletePersonEncounters(contactDiaryPersonEncounters: List<ContactDiaryPersonEncounter>) {
        Timber.d("Deleting person encounter $contactDiaryPersonEncounters")
        val contactDiaryPersonEncounterEntities = contactDiaryPersonEncounters
            .map {
                val contactDiaryPersonEncounterEntity = it.toContactDiaryPersonEncounterEntity()
                executeWhenIdNotDefault(contactDiaryPersonEncounterEntity.id)
                return@map contactDiaryPersonEncounterEntity
            }
        contactDiaryPersonEncounterDao.delete(contactDiaryPersonEncounterEntities)
    }

    override suspend fun deleteAllPersonEncounters() {
        Timber.d("Clearing contact diary person encounter table")
        contactDiaryPersonEncounterDao.deleteAll()
    }

    private suspend fun executeWhenIdNotDefault(id: Long, action: (suspend () -> Unit) = { }) {
        if (id != 0L) {
            action()
        } else {
            throw IllegalArgumentException("Entity has default id")
        }
    }

    override suspend fun clear() {
        Timber.d("Clearing contact diary database")
        contactDiaryDatabase.clearAllTables()
    }
}
