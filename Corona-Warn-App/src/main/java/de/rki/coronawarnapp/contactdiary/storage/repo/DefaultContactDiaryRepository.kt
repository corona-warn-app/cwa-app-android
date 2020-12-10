package de.rki.coronawarnapp.contactdiary.storage.repo

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationVisitDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonEncounterDao
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryPersonEntity
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultContactDiaryRepository @Inject constructor(
    private val contactDiaryLocationDao: ContactDiaryLocationDao,
    private val contactDiaryLocationVisitDao: ContactDiaryLocationVisitDao,
    private val contactDiaryPersonDao: ContactDiaryPersonDao,
    private val contactDiaryPersonEncounterDao: ContactDiaryPersonEncounterDao
) : ContactDiaryRepository {

    override val locations: Flow<List<ContactDiaryLocation>> = contactDiaryLocationDao.allEntries()
    override val people: Flow<List<ContactDiaryPerson>> = contactDiaryPersonDao.allEntries()


    //Location
    override suspend fun addLocation(contactDiaryLocation: ContactDiaryLocation) {
        Timber.d("Adding location $contactDiaryLocation")
        contactDiaryLocationDao.insert(contactDiaryLocation.toContactDiaryLocationEntity())
    }

    override suspend fun updateLocation(contactDiaryLocation: ContactDiaryLocation) {
        Timber.d("Updating location $contactDiaryLocation")
        val contactDiaryContactDiaryLocationEntity = contactDiaryLocation.toContactDiaryLocationEntity()
        executeWhenIdNotDefault(contactDiaryContactDiaryLocationEntity.locationId) {
            contactDiaryLocationDao.insert(contactDiaryContactDiaryLocationEntity)
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

    //Location visit
    override val locationVisits: Flow<List<ContactDiaryLocationVisit>>
        get() = TODO("Not yet implemented")

    override fun locationVisitsForDate(date: LocalDate): Flow<List<ContactDiaryLocationVisit>> {
        TODO("Not yet implemented")
    }

    override suspend fun addLocationVisit(contactDiaryLocationVisit: ContactDiaryLocationVisit) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLocationVisit(contactDiaryLocationVisit: ContactDiaryLocationVisit) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllLocationVisits() {
        TODO("Not yet implemented")
    }

    //Person
    override suspend fun addPerson(contactDiaryPerson: ContactDiaryPerson) {
        Timber.d("Adding person $contactDiaryPerson")
        contactDiaryPersonDao.insert(contactDiaryPerson.toContactDiaryPersonEntity())
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

    //Person encounter
    override val personEncounters: Flow<List<ContactDiaryPersonEncounter>>
        get() = TODO("Not yet implemented")

    override fun personEncountersForDate(date: LocalDate): Flow<List<ContactDiaryPersonEncounter>> {
        TODO("Not yet implemented")
    }

    override suspend fun addPersonEncounter(contactDiaryPersonEncounter: ContactDiaryPersonEncounter) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePersonEncounter(contactDiaryPersonEncounter: ContactDiaryPersonEncounter) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllPersonEncounters() {
        TODO("Not yet implemented")
    }

    private suspend fun executeWhenIdNotDefault(id: Long, action: (suspend () -> Unit) = { }) {
        if (id != 0L) {
            action()
        } else {
            throw IllegalArgumentException("Entity has default id")
        }
    }
}
