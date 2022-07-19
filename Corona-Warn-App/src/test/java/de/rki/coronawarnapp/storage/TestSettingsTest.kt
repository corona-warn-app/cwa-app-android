package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore

class TestSettingsTest : BaseTest() {

    private val objectMapper = SerializationModule().jacksonObjectMapper()
    private val dataStore = FakeDataStore()

    @AfterEach
    fun cleanup() {
        dataStore.reset()
    }

    private fun buildInstance(): TestSettings = TestSettings(
        dataStore = dataStore,
        objectMapper = objectMapper
    )

    @Test
    fun `get and update fake correct device time`() = runTest {
        dataStore[TestSettings.FAKE_CORRECT_DEVICE_TIME] shouldBe null

        with(buildInstance()) {
            fakeCorrectDeviceTime.first() shouldBe false
            updateFakeCorrectDeviceTime { !it }
            fakeCorrectDeviceTime.first() shouldBe true
            dataStore[TestSettings.FAKE_CORRECT_DEVICE_TIME] shouldBe true
        }
    }

    @Test
    fun `get and update fake metered connection`() = runTest {
        dataStore[TestSettings.FAKE_METERED_CONNECTION] shouldBe null

        with(buildInstance()) {
            fakeMeteredConnection.first() shouldBe false
            updateFakeMeteredConnection { !it }
            fakeMeteredConnection.first() shouldBe true
            dataStore[TestSettings.FAKE_METERED_CONNECTION] shouldBe true
        }
    }

    @Test
    fun `get and update fake exposure windows`() = runTest {
        dataStore[TestSettings.FAKE_EXPOSURE_WINDOWS_TYPE] shouldBe null

        with(buildInstance()) {
            fakeExposureWindows.first() shouldBe TestSettings.FakeExposureWindowTypes.DISABLED

            TestSettings.FakeExposureWindowTypes.values().forEach { type ->
                updateFakeExposureWindows(type)
                fakeExposureWindows.first() shouldBe type
                dataStore[TestSettings.FAKE_EXPOSURE_WINDOWS_TYPE] shouldBe "\"${type.name}\""
            }
        }
    }

    @Test
    fun `get and update skip safetynet time check`() = runTest {
        dataStore[TestSettings.SKIP_SAFETYNET_TIME_CHECK] shouldBe null

        with(buildInstance()) {
            skipSafetyNetTimeCheck.first() shouldBe false
            updateSkipSafetyNetTimeCheck { !it }
            skipSafetyNetTimeCheck.first() shouldBe true
            dataStore[TestSettings.SKIP_SAFETYNET_TIME_CHECK] shouldBe true
        }
    }
}
