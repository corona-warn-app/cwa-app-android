package de.rki.coronawarnapp.test.contactdiary.ui

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryRetentionCalculation
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import org.joda.time.LocalDate
import kotlin.random.Random

class ContactDiaryTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val repository: DefaultContactDiaryRepository,
    private val retentionCalculation: ContactDiaryRetentionCalculation
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val locationVisits = repository.locationVisits.asLiveData(context = dispatcherProvider.Default)
    val personEncounters = repository.personEncounters.asLiveData(context = dispatcherProvider.Default)

    fun getFancyLocationVisitString(list: List<ContactDiaryLocationVisit>): String {
        val sortedList = list.sortedBy { it.date }
        val builder = StringBuilder()
        for (entry in sortedList) {
            builder.append("[${entry.date.dayOfMonth}]")
        }
        return builder.toString()
    }

    fun getLocationVisitStatusString(list: List<ContactDiaryLocationVisit>): String {
        val filtered = retentionCalculation.filterContactDiaryLocationVisits(list)
        return "Outdated: ${filtered.count()} Normal: ${list.count() - filtered.count()} Total: ${list.count()}"
    }

    fun getFancyPersonEncounterString(list: List<ContactDiaryPersonEncounter>): String {
        val sortedList = list.sortedBy { it.date }
        val builder = StringBuilder()
        for (entry in sortedList) {
            builder.append("[${entry.date.dayOfMonth}]")
        }
        return builder.toString()
    }

    fun getPersonEncounterStatusString(list: List<ContactDiaryPersonEncounter>): String {
        val filtered = retentionCalculation.filterContactDiaryPersonEncounters(list)
        return "Outdated: ${filtered.count()} Normal: ${list.count() - filtered.count()} Total: ${list.count()}"
    }

    fun createLocationVisit(outdated: Boolean) {
        launch {
            val locationId = Random.nextLong()
            val contactDiaryLocation = DefaultContactDiaryLocation(locationId, "Test location $locationId")
            repository.addLocation(contactDiaryLocation)

            val locationVisit =
                DefaultContactDiaryLocationVisit(Random.nextLong(), getDate(outdated), contactDiaryLocation)
            repository.addLocationVisit(locationVisit)
        }
    }

    fun createPersonEncounters(outdated: Boolean) {
        launch {
            val contactPersonId = Random.nextLong()
            val contactPerson = DefaultContactDiaryPerson(contactPersonId, "Suspect #$contactPersonId")
            repository.addPerson(contactPerson)

            val personEncounter =
                DefaultContactDiaryPersonEncounter(Random.nextLong(), getDate(outdated), contactPerson)
            repository.addPersonEncounter(personEncounter)
        }
    }

    fun clearLocationVisits() {
        launch {
            retentionCalculation.clearObsoleteContactDiaryLocationVisits()
        }
    }

    fun clearPersonEncounters() {
        launch {
            retentionCalculation.clearObsoleteContactDiaryPersonEncounters()
        }
    }

    fun clearAll() {
        launch {
            repository.deleteAllLocationVisits()
            repository.deleteAllPersonEncounters()
            repository.deleteAllLocations()
            repository.deleteAllPeople()
        }
    }

    private fun getDate(outdated: Boolean): LocalDate {
        val date = LocalDate.now()
        return if (outdated) {
            date.minusDays(Random.nextInt(17, 25))
        } else {
            date.minusDays(Random.nextInt(0, 16))
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryTestFragmentViewModel>
}
