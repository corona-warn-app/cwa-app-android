package de.rki.coronawarnapp.ui.onboarding

import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.environment.BuildConfigWrap
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
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.FakeDataStore

@ExtendWith(InstantExecutorExtension::class)
class OnboardingAnalyticsViewModelTest : BaseTest() {

    lateinit var settings: AnalyticsSettings
    @MockK lateinit var analytics: Analytics
    @MockK lateinit var districts: Districts

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        settings = AnalyticsSettings(FakeDataStore())

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
    fun `accepting ppa updates versioncode and state `() = runTest2 {
        settings.lastOnboardingVersionCode.first() shouldBe 0L

        createInstance(scope = this).onProceed(true)

        coVerify { analytics.setAnalyticsEnabled(true) }
        settings.lastOnboardingVersionCode.first() shouldBe 1234567890L
    }

    @Test
    fun `declining ppa updates versioncode and state`() = runTest2 {
        settings.lastOnboardingVersionCode.first() shouldBe 0L

        createInstance(scope = this).onProceed(false)

        coVerify { analytics.setAnalyticsEnabled(false) }
        settings.lastOnboardingVersionCode.first() shouldBe 1234567890L
    }
}
