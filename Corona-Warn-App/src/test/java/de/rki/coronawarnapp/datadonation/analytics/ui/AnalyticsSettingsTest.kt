package de.rki.coronawarnapp.datadonation.analytics.ui

import android.content.Context
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class AnalyticsSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    lateinit var preferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        preferences = MockSharedPreferences()
        every { context.getSharedPreferences("analytics_localdata", Context.MODE_PRIVATE) } returns preferences
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
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
}
