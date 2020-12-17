package de.rki.coronawarnapp.contactdiary.retention

import dagger.Reusable
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.Days
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ContactDiaryRetentionCalculation @Inject constructor(
    private val timeStamper: TimeStamper,
    private val repository: DefaultContactDiaryRepository
) {

    fun getDaysDiff(dateSaved: LocalDate): Int {
        val today = LocalDate(timeStamper.nowUTC)
        return Days.daysBetween(dateSaved, today).days
    }

    fun filterContactDiaryLocationVisits(list: List<ContactDiaryLocationVisit>): List<ContactDiaryLocationVisit> {
        return list.filter { entity -> RETENTION_DAYS < getDaysDiff(entity.date) }
    }

    fun filterContactDiaryPersonEncounters(list: List<ContactDiaryPersonEncounter>): List<ContactDiaryPersonEncounter> {
        return list.filter { entity -> RETENTION_DAYS < getDaysDiff(entity.date) }
    }

    suspend fun clearObsoleteContactDiaryLocationVisits() {
        val list = repository.locationVisits.first()
        Timber.d("Contact Diary Location Visits total count: ${list.size}")
        val toDeleteList =
            list.filter { entity -> RETENTION_DAYS < getDaysDiff(entity.date).also { Timber.d("Days diff: $it") } }
        Timber.d("Contact Diary Location Visits to be deleted: ${toDeleteList.size}")
        repository.deleteLocationVisits(toDeleteList)
    }

    suspend fun clearObsoleteContactDiaryPersonEncounters() {
        val list = repository.personEncounters.first()
        Timber.d("Contact Diary Persons Encounters total count: ${list.size}")
        val toDeleteList =
            list.filter { entity -> RETENTION_DAYS < getDaysDiff(entity.date).also { Timber.d("Days diff: $it") } }
        Timber.d("Contact Diary Persons Encounters to be deleted: ${toDeleteList.size}")
        repository.deletePersonEncounters(toDeleteList)
    }

    companion object {
        /**
         * Contact diary data retention in days 14+2
         */
        const val RETENTION_DAYS = 16
    }
}
