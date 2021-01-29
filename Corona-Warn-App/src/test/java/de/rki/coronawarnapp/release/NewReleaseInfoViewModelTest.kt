package de.rki.coronawarnapp.release

import de.rki.coronawarnapp.main.CWASettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class NewReleaseInfoViewModelTest {

    @MockK lateinit var settings: CWASettings
    lateinit var viewModel: NewReleaseInfoViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { settings.lastChangelogVersion.update(any()) } just Runs
        viewModel = NewReleaseInfoViewModel(
            TestDispatcherProvider(),
            settings
        )
    }

    @Test
    fun testOnNextButtonClick() {
        viewModel.onNextButtonClick()
        viewModel.routeToScreen.value shouldBe NewReleaseInfoNavigationEvents.CloseScreen
    }

    @Test
    fun testCountryList() {
        val item1 = NewReleaseInfoItem("title", "body")
        val item2 = NewReleaseInfoItem("title2", "body2")
        val titles = arrayOf(item1.title, item2.title)
        val bodies = arrayOf(item1.body, item2.body)
        viewModel.getItems(titles, bodies) shouldBe listOf(item1, item2)
    }
}
