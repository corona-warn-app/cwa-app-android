package de.rki.coronawarnapp.ui.eventregistration.organizer

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.TraceLocationsFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.TraceLocationsViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import java.util.TimeZone

@RunWith(AndroidJUnit4::class)
class TraceLocationsFragmentTest : BaseUITest() {

    @MockK private lateinit var checkInsRepository: CheckInRepository
    @MockK private lateinit var traceLocationRepository: TraceLocationRepository

    private val timeZone = TimeZone.getTimeZone("Europe/Berlin")

    @Before
    fun setup() {
        TimeZone.setDefault(timeZone)
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(timeZone))
        MockKAnnotations.init(this, relaxed = true)

        every { checkInsRepository.allCheckIns } returns flowOf(listOf())

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

        onView(withId(R.id.description)).check(matches(withText("My Birthday Party")))
        onView(withId(R.id.address)).check(matches(withText("at my place")))
        onView(withId(R.id.duration)).check(matches(withText("19.04.21 06:12 - 22:52 Uhr")))
    }

    @Test
    fun itemWithDifferentDatesTest() {
        every { traceLocationRepository.traceLocationsWithinRetention } returns
            flowOf(listOf(TraceLocationData.traceLocationDifferentDate))

        launchFragmentInContainer2<TraceLocationsFragment>()

        onView(withId(R.id.description)).check(matches(withText("Your Birthday Party")))
        onView(withId(R.id.address)).check(matches(withText("at your place")))
        onView(withId(R.id.duration)).check(matches(withText("18.04.21 12:00 - 19.04.21 22:52 Uhr")))
    }

    private fun createViewModel() = TraceLocationsViewModel(
        checkInsRepository = checkInsRepository,
        traceLocationRepository = traceLocationRepository,
        dispatcherProvider = TestDispatcherProvider()
    )
}

@Module
abstract class TraceLocationsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun traceLocationsFragment(): TraceLocationsFragment
}
