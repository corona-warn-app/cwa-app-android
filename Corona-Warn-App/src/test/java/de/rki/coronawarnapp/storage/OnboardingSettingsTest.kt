package de.rki.coronawarnapp.storage

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.preferences.FakeDataStore
import java.time.Instant

class OnboardingSettingsTest : BaseIOTest() {

    private val dataStore = FakeDataStore()
    private val timestamp = Instant.parse("2020-01-01T14:00:00.000Z")

    private fun buildInstance(): OnboardingSettings = OnboardingSettings(
        dataStore = dataStore
    )

    @Test
    fun `onboardingCompletedTimestamp is correctly set`() = runTest {
        dataStore[OnboardingSettings.ONBOARDING_COMPLETED_TIMESTAMP] shouldBe null

        with(buildInstance()) {
            onboardingCompletedTimestamp.first() shouldBe null
            updateOnboardingCompletedTimestamp(timestamp)
            onboardingCompletedTimestamp.first() shouldBe timestamp
            dataStore[OnboardingSettings.ONBOARDING_COMPLETED_TIMESTAMP] shouldBe timestamp?.toEpochMilli()
        }
    }

    @Test
    fun `fabScannerOnboardingDone is correctly set`() = runTest {
        dataStore[OnboardingSettings.ONBOARDING_FAB_SCANNER_DONE] shouldBe null

        with(buildInstance()) {
            fabScannerOnboardingDone.first() shouldBe false
            updateFabScannerOnboardingDone(true)
            fabScannerOnboardingDone.first() shouldBe true
            dataStore[OnboardingSettings.ONBOARDING_FAB_SCANNER_DONE] shouldBe true
        }
    }

    @Test
    fun `exportAllOnboardingDone is correctly set`() = runTest {
        dataStore[OnboardingSettings.ONBOARDING_EXPORT_ALL_DONE] shouldBe null

        with(buildInstance()) {
            exportAllOnboardingDone.first() shouldBe false
            updateExportAllOnboardingDone(true)
            exportAllOnboardingDone.first() shouldBe true
            dataStore[OnboardingSettings.ONBOARDING_EXPORT_ALL_DONE] shouldBe true
        }
    }

    @Test
    fun `isBackgroundCheckDone is correctly set`() = runTest {
        dataStore[OnboardingSettings.BACKGROUND_CHECK_DONE] shouldBe null

        with(buildInstance()) {
            isBackgroundCheckDone.first() shouldBe false
            updateBackgroundCheckDone(true)
            isBackgroundCheckDone.first() shouldBe true
            dataStore[OnboardingSettings.BACKGROUND_CHECK_DONE] shouldBe true
        }
    }
}
