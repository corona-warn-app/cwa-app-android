package de.rki.coronawarnapp.release

import de.rki.coronawarnapp.main.CWASettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class NewReleaseInfoViewModelTest : BaseTest() {

    @MockK lateinit var appSettings: CWASettings
    lateinit var viewModel: NewReleaseInfoViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { appSettings.lastNotificationsOnboardingVersionCode } returns flowOf(0L)

        coEvery { appSettings.updateLastChangelogVersion(any()) } just Runs
        viewModel = NewReleaseInfoViewModel(
            TestDispatcherProvider(),
            appSettings
        )
    }

    @Test
    fun `if notifications onboarding has not yet been done, navigate to it`() = runTest {
        appSettings.lastNotificationsOnboardingVersionCode.first() shouldBe 0L

        viewModel.onNextButtonClick(false)
        viewModel.routeToScreen.value shouldBe NewReleaseInfoNavigationEvents
            .NavigateToOnboardingDeltaNotificationsFragment
    }

    @Test
    fun `if notifications onboarding is done, just close the release screen`() = runTest {
        every { appSettings.lastNotificationsOnboardingVersionCode } returns flowOf(1130000L)

        viewModel.onNextButtonClick(false)
        viewModel.routeToScreen.value shouldBe NewReleaseInfoNavigationEvents.CloseScreen

        appSettings.lastNotificationsOnboardingVersionCode.first() shouldBe 1130000L
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
