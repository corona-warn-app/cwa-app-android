package de.rki.coronawarnapp.storage

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.preferences.FakeDataStore
import java.time.LocalDate
import java.time.ZoneOffset

class TracingSettingsTest : BaseIOTest() {

    private val dataStore = FakeDataStore()
    private val date = LocalDate.parse("2020-11-09")

    private fun buildInstance(): TracingSettings = TracingSettings(
        dataStore = dataStore
    )

    @Test
    fun `isConsentGiven is correctly set`() = runTest {
        dataStore[TracingSettings.TRACING_ACTIVATION_TIMESTAMP] shouldBe null

        with(buildInstance()) {
            isConsentGiven() shouldBe false
            updateConsentGiven(true)
            isConsentGiven() shouldBe true
            dataStore[TracingSettings.TRACING_ACTIVATION_TIMESTAMP] shouldBe true
        }
    }

    @Test
    fun `isTestResultAvailableNotificationSentMigration is correctly set`() = runTest {
        dataStore[TracingSettings.TEST_RESULT_NOTIFICATION_SENT] shouldBe null

        with(buildInstance()) {
            isTestResultAvailableNotificationSentMigration() shouldBe false
            updateTestResultAvailableNotificationSentMigration(true)
            isTestResultAvailableNotificationSentMigration() shouldBe true
            dataStore[TracingSettings.TEST_RESULT_NOTIFICATION_SENT] shouldBe true
        }
    }

    @Test
    fun `isUserToBeNotifiedOfLoweredRiskLevel is correctly set`() = runTest {
        dataStore[TracingSettings.LOWERED_RISK_LEVEL] shouldBe null

        with(buildInstance()) {
            isUserToBeNotifiedOfLoweredRiskLevel.first() shouldBe false
            updateUserToBeNotifiedOfLoweredRiskLevel(true)
            isUserToBeNotifiedOfLoweredRiskLevel.first() shouldBe true
            dataStore[TracingSettings.LOWERED_RISK_LEVEL] shouldBe true
        }
    }

    @Test
    fun `isUserToBeNotifiedOfAdditionalHighRiskLevel is correctly set`() = runTest {
        dataStore[TracingSettings.ADDITIONAL_HIGH_RISK_LEVEL] shouldBe null

        with(buildInstance()) {
            isUserToBeNotifiedOfAdditionalHighRiskLevel.first() shouldBe false
            updateUserToBeNotifiedOfAdditionalHighRiskLevel(true)
            isUserToBeNotifiedOfAdditionalHighRiskLevel.first() shouldBe true
            dataStore[TracingSettings.ADDITIONAL_HIGH_RISK_LEVEL] shouldBe true
        }
    }

    @Test
    fun `lastHighRiskDate is correctly set`() = runTest {
        dataStore[TracingSettings.LAST_HIGH_RISK_LOCALDATE] shouldBe null

        with(buildInstance()) {
            lastHighRiskDate.first() shouldBe null
            updateLastHighRiskDate(date)

            lastHighRiskDate.first() shouldBe date
            dataStore[TracingSettings.LAST_HIGH_RISK_LOCALDATE] shouldBe
                date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        }
    }
}
