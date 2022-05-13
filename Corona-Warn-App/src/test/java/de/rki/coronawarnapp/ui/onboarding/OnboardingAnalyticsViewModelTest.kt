package de.rki.coronawarnapp.ui.onboarding

import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.PPAAgeGroup
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.PPAFederalState
import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class OnboardingAnalyticsViewModelTest : BaseTest() {

    @MockK lateinit var settings: AnalyticsSettings
    @MockK lateinit var analytics: Analytics
    @MockK lateinit var districts: Districts
    private lateinit var lastOnboardingVersionCode: FlowPreference<Long>

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        lastOnboardingVersionCode = mockFlowPreference(0L)

        every { settings.lastOnboardingVersionCode } returns lastOnboardingVersionCode
        every { settings.userInfoAgeGroup } returns mockFlowPreference(PPAAgeGroup.AGE_GROUP_UNSPECIFIED)
        every { settings.userInfoDistrict } returns mockFlowPreference(0)
        every { settings.userInfoFederalState } returns mockFlowPreference(PPAFederalState.FEDERAL_STATE_UNSPECIFIED)

        coEvery { analytics.setAnalyticsEnabled(any()) } just Runs

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 1234567890L
    }

    private fun createInstance(scope: CoroutineScope) = OnboardingAnalyticsViewModel(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        analytics = analytics,
        districts = districts,
        settings = settings
    )

    @Test
    fun `accepting ppa updates versioncode and state `() {
        lastOnboardingVersionCode.value shouldBe 0L

        runTest {
            createInstance(scope = this).onProceed(true)
        }

        coVerify { analytics.setAnalyticsEnabled(true) }
        lastOnboardingVersionCode.value shouldBe 1234567890L
    }

    @Test
    fun `declining ppa updates versioncode and state`() {
        lastOnboardingVersionCode.value shouldBe 0L

        runTest {
            createInstance(scope = this).onProceed(false)
        }

        coVerify { analytics.setAnalyticsEnabled(false) }
        lastOnboardingVersionCode.value shouldBe 1234567890L
    }
}
