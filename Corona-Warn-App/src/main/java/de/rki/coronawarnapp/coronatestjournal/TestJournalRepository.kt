package de.rki.coronawarnapp.coronatestjournal

import de.rki.coronawarnapp.coronatestjournal.storage.TestJournalStorage
import timber.log.Timber
import javax.inject.Inject

class TestJournalRepository @Inject constructor(private val storage: TestJournalStorage) {

    val tests = storage.getTests()

    suspend fun clear() {
        Timber.d("clear()")
        storage.deleteAll()
    }
}
