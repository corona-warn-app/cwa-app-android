package de.rki.coronawarnapp.datadonation.analytics.ui

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.preferences.FakeDataStore
import java.time.Instant

class AnalyticsSettingsTest : BaseTest() {

    lateinit var dataStore: FakeDataStore

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        dataStore = FakeDataStore()
    }

    fun createInstance() = AnalyticsSettings(
        dataStore = dataStore
    )

    @Test
    fun `userinfo agegroup`() = runTest2 {
        createInstance().apply {
            dataStore[AnalyticsSettings.PKEY_USERINFO_AGEGROUP] shouldBe null

            userInfoAgeGroup.first() shouldBe PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED
            updateUserInfoAgeGroup(PpaData.PPAAgeGroup.AGE_GROUP_FROM_60)
            dataStore[AnalyticsSettings.PKEY_USERINFO_AGEGROUP] shouldBe 3
            userInfoAgeGroup.first() shouldBe PpaData.PPAAgeGroup.AGE_GROUP_FROM_60

            updateUserInfoAgeGroup(PpaData.PPAAgeGroup.UNRECOGNIZED)
            userInfoAgeGroup.first() shouldBe PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED
        }
    }

    @Test
    fun `userinfo federal state`() = runTest2 {
        createInstance().apply {
            dataStore[AnalyticsSettings.PKEY_USERINFO_FEDERALSTATE] shouldBe null

            userInfoFederalState.first() shouldBe PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED
            updateUserInfoFederalState(PpaData.PPAFederalState.FEDERAL_STATE_NRW)
            dataStore[AnalyticsSettings.PKEY_USERINFO_FEDERALSTATE] shouldBe 10
            userInfoFederalState.first() shouldBe PpaData.PPAFederalState.FEDERAL_STATE_NRW

            updateUserInfoFederalState(PpaData.PPAFederalState.UNRECOGNIZED)
            userInfoFederalState.first() shouldBe PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED
        }
    }

    @Test
    fun `userinfo district`() = runTest2 {
        createInstance().apply {
            dataStore[AnalyticsSettings.PKEY_USERINFO_DISTRICT] shouldBe null

            userInfoDistrict.first() shouldBe 0
            updateUserInfoDistrict(123)
            dataStore[AnalyticsSettings.PKEY_USERINFO_DISTRICT] shouldBe 123

            userInfoDistrict.first() shouldBe 123
        }
    }

    @Test
    fun `exposure risk metadata serialisation`() = runTest2 {
        createInstance().apply {
            dataStore[AnalyticsSettings.PREVIOUS_EXPOSURE_RISK_METADATA] shouldBe null

            previousExposureRiskMetadata.first() shouldBe null

            val metadata = PpaData.ExposureRiskMetadata.newBuilder()
                .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
                .setMostRecentDateAtRiskLevel(Instant.ofEpochSecond(101010).toEpochMilli())
                .setDateChangedComparedToPreviousSubmission(true)
                .setRiskLevelChangedComparedToPreviousSubmission(true)
                .build()

            updatePreviousExposureRiskMetadata(metadata)

            dataStore[AnalyticsSettings.PREVIOUS_EXPOSURE_RISK_METADATA] shouldBe "CAMQARjQlJUwIAE="

            previousExposureRiskMetadata.first() shouldBe metadata
        }
    }

    @Test
    fun `exposure risk metadata invalid proto handling`() = runTest2 {
        createInstance().apply {
            dataStore[AnalyticsSettings.PREVIOUS_EXPOSURE_RISK_METADATA] shouldBe null

            previousExposureRiskMetadata.first() shouldBe null

            // If ExposureRiskMetadata is changed this test will fail, we need some kind of migration strategy then
            val validProto = "CAMQARjQlJUwIAE="

            dataStore[AnalyticsSettings.PREVIOUS_EXPOSURE_RISK_METADATA] = validProto

            val metadata = PpaData.ExposureRiskMetadata.newBuilder()
                .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
                .setMostRecentDateAtRiskLevel(Instant.ofEpochSecond(101010).toEpochMilli())
                .setDateChangedComparedToPreviousSubmission(true)
                .setRiskLevelChangedComparedToPreviousSubmission(true)
                .build()

            previousExposureRiskMetadata.first() shouldBe metadata
        }
    }
}
