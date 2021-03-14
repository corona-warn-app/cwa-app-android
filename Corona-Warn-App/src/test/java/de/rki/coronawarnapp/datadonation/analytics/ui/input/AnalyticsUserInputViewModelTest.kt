package de.rki.coronawarnapp.datadonation.analytics.ui.input

import android.content.Context
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment.InputType
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.PPAAgeGroup
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.PPAFederalState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.asDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class AnalyticsUserInputViewModelTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var districtsSource: Districts
    @MockK lateinit var analyticsSettings: AnalyticsSettings

    private val userInfoAgeGroup = mockFlowPreference(PPAAgeGroup.AGE_GROUP_UNSPECIFIED)
    private val userInfoFederalState = mockFlowPreference(PPAFederalState.FEDERAL_STATE_UNSPECIFIED)
    private var userInfoDistrict = mockFlowPreference(0)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.getString(any()) } returns ""

        every { analyticsSettings.userInfoAgeGroup } returns userInfoAgeGroup
        every { analyticsSettings.userInfoFederalState } returns userInfoFederalState
        every { analyticsSettings.userInfoDistrict } returns userInfoDistrict

        coEvery { districtsSource.loadDistricts() } returns emptyList()
    }

    fun createInstance(
        inputType: InputType,
        scope: CoroutineScope
    ) = AnalyticsUserInputViewModel(
        type = inputType,
        dispatcherProvider = scope.asDispatcherProvider(),
        context = context,
        districtsSource = districtsSource,
        settings = analyticsSettings
    )

    @Test
    fun `test agegroup emission`() = runBlockingTest2(ignoreActive = true) {
        userInfoAgeGroup.update { PPAAgeGroup.AGE_GROUP_0_TO_29 }
        val instance = createInstance(inputType = InputType.AGE_GROUP, scope = this)

        instance.userInfoItems.observeForever { }
        instance.userInfoItems.value!![0].apply {
            data shouldBe PPAAgeGroup.AGE_GROUP_UNSPECIFIED
            isSelected shouldBe false
        }
        instance.userInfoItems.value!![1].apply {
            data shouldBe PPAAgeGroup.AGE_GROUP_0_TO_29
            isSelected shouldBe true
        }
        instance.userInfoItems.value!![3].apply {
            data shouldBe PPAAgeGroup.AGE_GROUP_FROM_60
            isSelected shouldBe false
        }
    }

    @Test
    fun `test agegroup selection`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(inputType = InputType.AGE_GROUP, scope = this)
        instance.finishEvent.value shouldBe null

        instance.selectUserInfoItem(
            UserInfoItem(data = PPAAgeGroup.AGE_GROUP_30_TO_59, label = mockk(), isSelected = false)
        )

        userInfoAgeGroup.value shouldBe PPAAgeGroup.AGE_GROUP_30_TO_59
        userInfoFederalState.value shouldBe PPAFederalState.FEDERAL_STATE_UNSPECIFIED
        userInfoDistrict.value shouldBe 0

        instance.finishEvent.value shouldBe Unit
    }

    @Test
    fun `test federal state emission`() = runBlockingTest2(ignoreActive = true) {
        userInfoFederalState.update { PPAFederalState.FEDERAL_STATE_HH }
        val instance = createInstance(inputType = InputType.FEDERAL_STATE, scope = this)

        instance.userInfoItems.observeForever { }
        instance.userInfoItems.value!![0].apply {
            data shouldBe PPAFederalState.FEDERAL_STATE_UNSPECIFIED
            isSelected shouldBe false
        }
        instance.userInfoItems.value!![1].apply {
            data shouldBe PPAFederalState.FEDERAL_STATE_BW
            isSelected shouldBe false
        }
        instance.userInfoItems.value!![6].apply {
            data shouldBe PPAFederalState.FEDERAL_STATE_HH
            isSelected shouldBe true
        }
        instance.userInfoItems.value!![16].apply {
            data shouldBe PPAFederalState.FEDERAL_STATE_TH
            isSelected shouldBe false
        }
    }

    @Test
    fun `test federal state selection`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(inputType = InputType.FEDERAL_STATE, scope = this)

        instance.finishEvent.value shouldBe null
        userInfoDistrict.update { 12345 } // Because federal state selection should reset this

        instance.selectUserInfoItem(
            UserInfoItem(data = PPAFederalState.FEDERAL_STATE_NRW, label = mockk(), isSelected = false)
        )

        userInfoAgeGroup.value shouldBe PPAAgeGroup.AGE_GROUP_UNSPECIFIED
        userInfoFederalState.value shouldBe PPAFederalState.FEDERAL_STATE_NRW
        userInfoDistrict.value shouldBe 0

        instance.finishEvent.value shouldBe Unit
    }

    @Test
    fun `test district emission`() = runBlockingTest2(ignoreActive = true) {
        userInfoFederalState.update { PPAFederalState.FEDERAL_STATE_NRW }

        val ourDistrict = Districts.District(
            districtId = 1234,
            federalStateShortName = "NW"
        )
        val notOurDistrict = Districts.District(
            districtId = 5678,
            federalStateShortName = "BE"
        )
        coEvery { districtsSource.loadDistricts() } returns listOf(ourDistrict, notOurDistrict)

        val instance = createInstance(inputType = InputType.DISTRICT, scope = this)

        instance.userInfoItems.observeForever { }
        instance.userInfoItems.value!![0].data shouldBe Districts.District()
        instance.userInfoItems.value!![1].data shouldBe ourDistrict
    }

    @Test
    fun `test district selection`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(inputType = InputType.DISTRICT, scope = this)
        instance.finishEvent.value shouldBe null

        instance.selectUserInfoItem(
            UserInfoItem(data = Districts.District(districtId = 9000), label = mockk(), isSelected = false)
        )
        userInfoAgeGroup.value shouldBe PPAAgeGroup.AGE_GROUP_UNSPECIFIED
        userInfoFederalState.value shouldBe PPAFederalState.FEDERAL_STATE_UNSPECIFIED
        userInfoDistrict.value shouldBe 9000

        instance.finishEvent.value shouldBe Unit
    }
}
