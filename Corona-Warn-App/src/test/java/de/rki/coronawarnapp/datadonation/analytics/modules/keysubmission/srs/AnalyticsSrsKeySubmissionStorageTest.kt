package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.srs

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore

internal class AnalyticsSrsKeySubmissionStorageTest : BaseTest() {

    private val dataStore = FakeDataStore()

    @Test
    fun reset() = runTest {
        AnalyticsSrsKeySubmissionStorage(dataStore).apply {
            saveSrsPpaData("ABBA")
            dataStore[AnalyticsSrsKeySubmissionStorage.SRS_PPA_DATA] shouldBe "ABBA"
            reset()

            dataStore[AnalyticsSrsKeySubmissionStorage.SRS_PPA_DATA] shouldBe null
        }
    }

    @Test
    fun saveSrsPpaData() = runTest {
        AnalyticsSrsKeySubmissionStorage(dataStore).apply {
            saveSrsPpaData("ABBA")
            dataStore[AnalyticsSrsKeySubmissionStorage.SRS_PPA_DATA] shouldBe "ABBA"
        }
    }

    @Test
    fun getSrsPpaData() = runTest {
        AnalyticsSrsKeySubmissionStorage(dataStore).apply {
            getSrsPpaData() shouldBe null
            saveSrsPpaData("ABBA")
            dataStore[AnalyticsSrsKeySubmissionStorage.SRS_PPA_DATA] shouldBe "ABBA"
            getSrsPpaData() shouldBe "ABBA"
        }
    }
}
