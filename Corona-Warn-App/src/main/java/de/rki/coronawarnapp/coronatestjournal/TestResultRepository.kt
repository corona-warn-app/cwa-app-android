package de.rki.coronawarnapp.coronatestjournal

import de.rki.coronawarnapp.coronatestjournal.storage.TestResultStorage
import javax.inject.Inject

class TestResultRepository @Inject constructor(storage: TestResultStorage) {

    val tests = storage.getTests()
}
