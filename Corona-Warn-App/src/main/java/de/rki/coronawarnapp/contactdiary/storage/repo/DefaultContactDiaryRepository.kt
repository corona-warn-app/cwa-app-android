package de.rki.coronawarnapp.contactdiary.storage.repo

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryElement
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonDao
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementLocationXRef
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementPersonXRef
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryDateEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryPersonEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultContactDiaryRepository @Inject constructor(
    private val contactDiaryLocationDao: ContactDiaryLocationDao,
    private val contactDiaryPersonDao: ContactDiaryPersonDao
) : ContactDiaryRepository {

    override val contactDiaryDates: Flow<List<LocalDate>> = contactDiaryDateDao
        .allEntries()
        .map { it.map { contactDiaryDateEntity -> contactDiaryDateEntity.date } }

    override val contactDiaryElements: Flow<List<ContactDiaryElement>> = contactDiaryElementDao.allEntries()
    override val locations: Flow<List<ContactDiaryLocation>> = contactDiaryLocationDao.allEntries()
    override val people: Flow<List<ContactDiaryPerson>> = contactDiaryPersonDao.allEntries()

    //Date
    override suspend fun addDate(date: LocalDate) {
        Timber.d("Adding date $date")
        contactDiaryDateDao.insert(date.toContactDiaryDateEntity())
    }

    override suspend fun addDates(dates: List<LocalDate>) {
        Timber.d("Adding dates $dates")
        contactDiaryDateDao.insertAll(dates.map { it.toContactDiaryDateEntity() })
    }

    override suspend fun deleteDate(date: LocalDate) {
        Timber.d("Deleting date $date")
        contactDiaryDateDao.delete(date.toContactDiaryDateEntity())
    }

    override suspend fun deleteAllDates() {
        Timber.d("Clearing contact diary date table")
        contactDiaryDateDao.deleteAll()
    }

    //ContactDiaryElement
    private suspend fun ContactDiaryPerson.toContactDiaryElementPersonXRef(date: LocalDate): ContactDiaryElementPersonXRef {
        val personId = this.toContactDiaryPersonEntity().personId
        executeWhenIdNotDefault(personId)
        return ContactDiaryElementPersonXRef(date, personId)
    }

    private suspend fun ContactDiaryLocation.toContactDiaryElementLocationXRef(date: LocalDate): ContactDiaryElementLocationXRef {
        val locationId = this.toContactDiaryLocationEntity().locationId
        executeWhenIdNotDefault(locationId)
        return ContactDiaryElementLocationXRef(date, locationId)
    }

    private suspend fun List<ContactDiaryPerson>.toContactDiaryElementPersonXRefs(date: LocalDate): List<ContactDiaryElementPersonXRef> =
        this.map { it.toContactDiaryElementPersonXRef(date) }

    private suspend fun List<ContactDiaryLocation>.toContactDiaryElementLocationXRefs(date: LocalDate): List<ContactDiaryElementLocationXRef> =
        this.map { it.toContactDiaryElementLocationXRef(date) }

    override suspend fun addPersonToDate(contactDiaryPerson: ContactDiaryPerson, date: LocalDate) {
        Timber.d("Adding person $contactDiaryPerson to date $date")
        val contactDiaryElementPersonXRef = contactDiaryPerson.toContactDiaryElementPersonXRef(date)
        contactDiaryElementDao.insertContactDiaryElementPersonXRef(contactDiaryElementPersonXRef)
    }

    override suspend fun addPeopleToDate(contactDiaryPeople: List<ContactDiaryPerson>, date: LocalDate) {
        Timber.d("Adding people $contactDiaryPeople to date $date")
        val contactDiaryElementPersonXRefs = contactDiaryPeople.toContactDiaryElementPersonXRefs(date)
        contactDiaryElementDao.insertContactDiaryElementPersonXRefs(contactDiaryElementPersonXRefs)
    }

    override suspend fun addLocationToDate(contactDiaryLocation: ContactDiaryLocation, date: LocalDate) {
        Timber.d("Adding location $contactDiaryLocation to date $date")
        val contactDiaryElementLocationXRef = contactDiaryLocation.toContactDiaryElementLocationXRef(date)
        contactDiaryElementDao.insertContactDiaryElementLocationXRef(contactDiaryElementLocationXRef)
    }

    override suspend fun addLocationsToDate(contactDiaryLocations: List<ContactDiaryLocation>, date: LocalDate) {
        Timber.d("Adding locations $contactDiaryLocations to date $date")
        val contactDiaryElementLocationXRefs = contactDiaryLocations.toContactDiaryElementLocationXRefs(date)
        contactDiaryElementDao.insertContactDiaryElementLocationXRefs(contactDiaryElementLocationXRefs)
    }

    override suspend fun removePersonFromDate(contactDiaryPerson: ContactDiaryPerson, date: LocalDate) {
        Timber.d("Removing person $contactDiaryPerson from date $date")
        val contactDiaryElementPersonXRef = contactDiaryPerson.toContactDiaryElementPersonXRef(date)
        contactDiaryElementDao.deleteContactDiaryElementPersonXRef(contactDiaryElementPersonXRef)
    }

    override suspend fun removePeopleFromDate(contactDiaryPeople: List<ContactDiaryPerson>, date: LocalDate) {
        Timber.d("Removing people $contactDiaryPeople from date $date")
        val contactDiaryElementPersonXRefs = contactDiaryPeople.toContactDiaryElementPersonXRefs(date)
        contactDiaryElementDao.deleteContactDiaryElementPersonXRefs(contactDiaryElementPersonXRefs)
    }

    override suspend fun removeLocationFromDate(contactDiaryLocation: ContactDiaryLocation, date: LocalDate) {
        Timber.d("Removing location $contactDiaryLocation from date $date")
        val contactDiaryElementLocationXRef = contactDiaryLocation.toContactDiaryElementLocationXRef(date)
        contactDiaryElementDao.deleteContactDiaryElementLocationXRef(contactDiaryElementLocationXRef)
    }

    override suspend fun removeLocationsFromDate(contactDiaryLocations: List<ContactDiaryLocation>, date: LocalDate) {
        Timber.d("Removing location $contactDiaryLocations from date $date")
        val contactDiaryElementLocationXRefs = contactDiaryLocations.toContactDiaryElementLocationXRefs(date)
        contactDiaryElementDao.deleteContactDiaryElementLocationXRefs(contactDiaryElementLocationXRefs)
    }

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

    private suspend fun executeWhenIdNotDefault(id: Long, action: (suspend () -> Unit) = { }) {
        if (id != 0L) {
            action()
        } else {
            throw IllegalArgumentException("Entity has default id")
        }
    }
}
