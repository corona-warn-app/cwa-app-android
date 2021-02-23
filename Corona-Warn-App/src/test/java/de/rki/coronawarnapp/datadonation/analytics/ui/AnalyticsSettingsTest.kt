package de.rki.coronawarnapp.datadonation.analytics.ui

import android.content.Context
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences
import java.time.Instant

class AnalyticsSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    lateinit var preferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        preferences = MockSharedPreferences()
        every { context.getSharedPreferences("analytics_localdata", Context.MODE_PRIVATE) } returns preferences
    }

    fun createInstance() = AnalyticsSettings(
        context = context
    )

    @Test
    fun `userinfo agegroup`() {
        createInstance().apply {
            preferences.dataMapPeek.isEmpty() shouldBe true

            userInfoAgeGroup.value shouldBe PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED
            userInfoAgeGroup.update { PpaData.PPAAgeGroup.AGE_GROUP_FROM_60 }
            preferences.dataMapPeek["userinfo.agegroup"] shouldBe 3
            userInfoAgeGroup.value shouldBe PpaData.PPAAgeGroup.AGE_GROUP_FROM_60

            userInfoAgeGroup.update { PpaData.PPAAgeGroup.UNRECOGNIZED }
            userInfoAgeGroup.value shouldBe PpaData.PPAAgeGroup.AGE_GROUP_UNSPECIFIED
        }
    }

    @Test
    fun `userinfo federal state`() {
        createInstance().apply {
            preferences.dataMapPeek.isEmpty() shouldBe true

            userInfoFederalState.value shouldBe PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED
            userInfoFederalState.update { PpaData.PPAFederalState.FEDERAL_STATE_NRW }
            preferences.dataMapPeek["userinfo.federalstate"] shouldBe 10
            userInfoFederalState.value shouldBe PpaData.PPAFederalState.FEDERAL_STATE_NRW

            userInfoFederalState.update { PpaData.PPAFederalState.UNRECOGNIZED }
            userInfoFederalState.value shouldBe PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED
        }
    }

    @Test
    fun `userinfo district`() {
        createInstance().apply {
            preferences.dataMapPeek.isEmpty() shouldBe true

            userInfoDistrict.value shouldBe 0
            userInfoDistrict.update { 123 }
            preferences.dataMapPeek["userinfo.district"] shouldBe 123

            userInfoDistrict.value shouldBe 123
        }
    }

    @Test
    fun `exposure risk metadata serialisation`() {
        createInstance().apply {
            preferences.dataMapPeek.isEmpty() shouldBe true

            previousExposureRiskMetadata.value shouldBe null

            val metadata = PpaData.ExposureRiskMetadata.newBuilder()
                .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
                .setMostRecentDateAtRiskLevel(Instant.ofEpochSecond(101010).toEpochMilli())
                .setDateChangedComparedToPreviousSubmission(true)
                .setRiskLevelChangedComparedToPreviousSubmission(true)
                .build()

            previousExposureRiskMetadata.update {
                metadata
            }

            preferences.dataMapPeek["exposurerisk.metadata.previous"] shouldBe "CAMQARjQlJUwIAE="

            previousExposureRiskMetadata.value shouldBe metadata
        }
    }

    @Test
    fun `exposure risk metadata invalid proto handling`() {
        createInstance().apply {
            preferences.dataMapPeek.isEmpty() shouldBe true

            previousExposureRiskMetadata.value shouldBe null

            // If ExposureRiskMetadata is changed this test will fail, we need some kind of migration strategy then
            val validProto = "CAMQARjQlJUwIAE="

            preferences.edit().putString("exposurerisk.metadata.previous", validProto).commit()

            val metadata = PpaData.ExposureRiskMetadata.newBuilder()
                .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
                .setMostRecentDateAtRiskLevel(Instant.ofEpochSecond(101010).toEpochMilli())
                .setDateChangedComparedToPreviousSubmission(true)
                .setRiskLevelChangedComparedToPreviousSubmission(true)
                .build()

            previousExposureRiskMetadata.value shouldBe metadata
        }
    }
}
