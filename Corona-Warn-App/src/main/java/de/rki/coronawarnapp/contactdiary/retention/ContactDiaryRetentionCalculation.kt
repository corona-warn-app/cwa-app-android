package de.rki.coronawarnapp.contactdiary.retention

import dagger.Reusable
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset
import javax.inject.Inject

@Reusable
class ContactDiaryRetentionCalculation @Inject constructor(
    private val timeStamper: TimeStamper,
    private val repository: DefaultContactDiaryRepository,
    private val riskLevelStorage: RiskLevelStorage
) {

    fun isOutOfRetention(date: LocalDate): Boolean = RETENTION_DAYS < getDaysDiff(date).also {
        Timber.d("Days diff: $it")
    }

    fun getDaysDiff(dateSaved: LocalDate): Long {
        val today: LocalDate = timeStamper.nowUTC.atZone(ZoneOffset.UTC).toLocalDate()
        return Period.between(dateSaved, today).days.toLong()
    }

    fun filterContactDiaryLocationVisits(list: List<ContactDiaryLocationVisit>): List<ContactDiaryLocationVisit> {
        return list.filter { entity -> isOutOfRetention(entity.date) }
    }

    fun filterContactDiaryPersonEncounters(list: List<ContactDiaryPersonEncounter>): List<ContactDiaryPersonEncounter> {
        return list.filter { entity -> isOutOfRetention(entity.date) }
    }

    suspend fun clearObsoleteContactDiaryLocationVisits() {
        val list = repository.locationVisits.first()
        Timber.d("Contact Diary Location Visits total count: ${list.size}")
        val toDeleteList = list.filter { entity -> isOutOfRetention(entity.date) }
        Timber.d("Contact Diary Location Visits to be deleted: ${toDeleteList.size}")
        repository.deleteLocationVisits(toDeleteList)
    }

    suspend fun clearObsoleteContactDiaryPersonEncounters() {
        val list = repository.personEncounters.first()
        Timber.d("Contact Diary Persons Encounters total count: ${list.size}")
        val toDeleteList = list.filter { entity -> isOutOfRetention(entity.date) }
        Timber.d("Contact Diary Persons Encounters to be deleted: ${toDeleteList.size}")
        repository.deletePersonEncounters(toDeleteList)
    }

    suspend fun clearObsoleteRiskPerDate() {
        val list = riskLevelStorage.ewDayRiskStates.first()
        Timber.d("Aggregated Risk Per Date Results total count: ${list.size}")
        val toDeleteList = list.filter { risk -> isOutOfRetention(risk.localDateUtc) }
        Timber.d("AggregatedRiskPerDateResult to be deleted count: ${toDeleteList.size}")
        riskLevelStorage.deleteAggregatedRiskPerDateResults(toDeleteList)
    }

    suspend fun clearObsoleteCoronaTests() {
        repository.testResults.first()
            .also { Timber.d("Contact Diary Corona Tests total count: %d", it.size) }
            .filter { isOutOfRetention(it.time.toLocalDateUtc()) }
            .also {
                Timber.d("Contact Diary Corona Tests to be deleted: %d", it.size)
                repository.deleteTests(it)
            }
    }

    companion object {
        /**
         * Contact diary data retention in days 15+1
         */
        const val RETENTION_DAYS = 16
    }
}
