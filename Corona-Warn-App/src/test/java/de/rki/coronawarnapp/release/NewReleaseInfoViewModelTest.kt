package de.rki.coronawarnapp.release

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class NewReleaseInfoViewModelTest : BaseTest() {

    @MockK lateinit var appSettings: CWASettings
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    private lateinit var lastOnboardingVersionCode: FlowPreference<Long>
    lateinit var viewModel: NewReleaseInfoViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        lastOnboardingVersionCode = mockFlowPreference(0L)
        every { analyticsSettings.lastOnboardingVersionCode } returns lastOnboardingVersionCode

        every { appSettings.lastChangelogVersion.update(any()) } just Runs
        viewModel = NewReleaseInfoViewModel(
            TestDispatcherProvider(),
            appSettings,
            analyticsSettings
        )
    }

    @Test
    fun `if analytics onboarding has not yet been done, navigate to it`() {
        lastOnboardingVersionCode.value shouldBe 0L

        viewModel.onNextButtonClick()
        viewModel.routeToScreen.value shouldBe NewReleaseInfoNavigationEvents.NavigateToOnboardingDeltaAnalyticsFragment
    }

    @Test
    fun `if analytics onboarding is done, just close the release screen`() {
        lastOnboardingVersionCode.update { 1130000L }

        viewModel.onNextButtonClick()
        viewModel.routeToScreen.value shouldBe NewReleaseInfoNavigationEvents.CloseScreen

        lastOnboardingVersionCode.value shouldBe 1130000L
    }

    @Test
    fun `title and body shall be mapped to list of items`() {
        val item1 = NewReleaseInfoItemText("title", "body")
        val item2 = NewReleaseInfoItemText("title2", "body2")
        val titles = arrayOf(item1.title, item2.title)
        val bodies = arrayOf(item1.body, item2.body)
        viewModel.getItems(titles, bodies, arrayOf("", ""), arrayOf("", "")) shouldBe listOf(item1, item2)
    }

    @Test
    fun `missing title results in an empty list`() {
        val item1 = NewReleaseInfoItemText("title", "body")
        val item2 = NewReleaseInfoItemText("title2", "body2")
        val titles = arrayOf(item1.title)
        val bodies = arrayOf(item1.body, item2.body)
        viewModel.getItems(titles, bodies, arrayOf("", ""), arrayOf("", "")) shouldBe emptyList()
    }

    @Test
    fun `missing body results in an empty list`() {
        val item1 = NewReleaseInfoItemText("title", "body")
        val item2 = NewReleaseInfoItemText("title2", "body2")
        val titles = arrayOf(item1.title, item2.title)
        val bodies = arrayOf(item1.body)
        viewModel.getItems(titles, bodies, arrayOf("", ""), arrayOf("", "")) shouldBe emptyList()
    }

    @Test
    fun `missing linkified label results in an empty list`() {
        val item1 = NewReleaseInfoItemText("title", "body")
        val item2 = NewReleaseInfoItemText("title2", "body2")
        val titles = arrayOf(item1.title, item2.title)
        val bodies = arrayOf(item1.body, item2.body)
        viewModel.getItems(titles, bodies, arrayOf(""), arrayOf("", "")) shouldBe emptyList()
    }

    @Test
    fun `missing target url results in an empty list`() {
        val item1 = NewReleaseInfoItemText("title", "body")
        val item2 = NewReleaseInfoItemText("title2", "body2")
        val titles = arrayOf(item1.title, item2.title)
        val bodies = arrayOf(item1.body, item2.body)
        viewModel.getItems(titles, bodies, arrayOf("", ""), arrayOf("")) shouldBe emptyList()
    }

    @Test
    fun `items with and without links are mapped successfully`() {
        val item1 = NewReleaseInfoItemText("title", "body")
        val item2 = NewReleaseInfoItemLinked("title2", "body2", "label2", "url2")
        val titles = arrayOf(item1.title, item2.title)
        val bodies = arrayOf(item1.body, item2.body)
        val linkifiedLabels = arrayOf("", item2.linkifiedLabel)
        val linkTargets = arrayOf("", item2.linkTarget)
        viewModel.getItems(titles, bodies, linkifiedLabels, linkTargets) shouldBe listOf(item1, item2)
    }
}
