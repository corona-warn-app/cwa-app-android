package de.rki.coronawarnapp.ui.eventregistration.organizer

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.TraceLocationsFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.TraceLocationsViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.util.TimeZone

@RunWith(AndroidJUnit4::class)
class TraceLocationsFragmentTest : BaseUITest() {

    @MockK private lateinit var checkInsRepository: CheckInRepository
    @MockK private lateinit var traceLocationRepository: TraceLocationRepository
    @MockK lateinit var traceLocationSettings: TraceLocationSettings

    private val timeZone = TimeZone.getTimeZone("Europe/Berlin")

    @Before
    fun setup() {
        TimeZone.setDefault(timeZone)
        MockKAnnotations.init(this, relaxed = true)

        every { checkInsRepository.allCheckIns } returns flowOf(listOf())
        every { traceLocationSettings.onboardingStatus } returns
            flowOf(TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0)

        setupMockViewModel(
            object : TraceLocationsViewModel.Factory {
                override fun create(): TraceLocationsViewModel {
                    return createViewModel()
                }
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun itemWithSameDatesTest() {
        every { traceLocationRepository.traceLocationsWithinRetention } returns
            flowOf(listOf(TraceLocationData.traceLocationSameDate))

        launchFragmentInContainer2<TraceLocationsFragment>()

        onView(withId(R.id.trace_locations_item_description)).check(matches(withText("Jahrestreffen der deutschen SAP Anwendergruppe")))
        onView(withId(R.id.trace_locations_item_address)).check(matches(withText("Hauptstr. 3, 69115 Heidelberg")))
        onView(withId(R.id.duration)).check(matches(withText("21.06.21, 18:00 - 21:00 Uhr")))
    }

    @Test
    fun itemWithDifferentDatesTest() {
        every { traceLocationRepository.traceLocationsWithinRetention } returns
            flowOf(listOf(TraceLocationData.traceLocationDifferentDate))

        launchFragmentInContainer2<TraceLocationsFragment>()

        onView(withId(R.id.trace_locations_item_description)).check(matches(withText("Event XYZ")))
        onView(withId(R.id.trace_locations_item_address)).check(matches(withText("Otto-Hahn-Str. 3, 123456 Berlin")))
        onView(withId(R.id.duration)).check(matches(withText("18.04.21, 12:00 - 19.04.21, 22:52 Uhr")))
    }

    @Screenshot
    @Test
    fun screenshot_menu() {
        every { traceLocationRepository.traceLocationsWithinRetention } returns flowOf(listOf())

        launchFragmentInContainer2<TraceLocationsFragment>()
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        takeScreenshot<TraceLocationsFragment>("_menu")
    }

    private fun createViewModel() = TraceLocationsViewModel(
        appScope = TestScope(),
        dispatcherProvider = TestDispatcherProvider(),
        checkInsRepository = checkInsRepository,
        traceLocationRepository = traceLocationRepository,
        settings = traceLocationSettings
    )
}

@Module
abstract class TraceLocationsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun traceLocationsFragment(): TraceLocationsFragment
}
