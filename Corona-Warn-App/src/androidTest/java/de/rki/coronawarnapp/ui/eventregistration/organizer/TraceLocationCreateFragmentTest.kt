package de.rki.coronawarnapp.ui.eventregistration.organizer

import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.locations.TraceLocationCreator
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategory
import de.rki.coronawarnapp.ui.presencetracing.organizer.create.TraceLocationCreateFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.create.TraceLocationCreateFragmentArgs
import de.rki.coronawarnapp.ui.presencetracing.organizer.create.TraceLocationCreateViewModel
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
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
class TraceLocationCreateFragmentTest : BaseUITest() {

    @MockK private lateinit var traceLocationRepository: TraceLocationRepository
    @MockK private lateinit var traceLocationCreator: TraceLocationCreator

    private val timeZone = TimeZone.getTimeZone("Europe/Berlin")

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread { setGraph(R.navigation.trace_location_organizer_nav_graph) }
    }

    @Before
    fun setup() {
        TimeZone.setDefault(timeZone)
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(timeZone))
        MockKAnnotations.init(this, relaxed = true)

        coEvery { traceLocationRepository.addTraceLocation(any()) } returns TraceLocationData.traceLocationSameDate

        setupMockViewModel(
            object : TraceLocationCreateViewModel.Factory {
                override fun create(category: TraceLocationCategory): TraceLocationCreateViewModel {
                    return createViewModel(category)
                }
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun duplicateEventWithSameDatesTest() {
        launchFragmentInContainer2<TraceLocationCreateFragment>(
            fragmentArgs = TraceLocationCreateFragmentArgs(
                category = TraceLocationData.categoryEvent,
                originalItem = TraceLocationData.traceLocationSameDate
            ).toBundle()
        ).onFragment { fragment -> Navigation.setViewNavController(fragment.requireView(), navController) }

        onView(withId(R.id.description_input_edit)).check(matches(withText("My Birthday Party")))
        onView(withId(R.id.place_input_edit)).check(matches(withText("at my place")))
        onView(withId(R.id.value_start)).check(matches(withText("Mo., 19.04.21   06:12")))
        onView(withId(R.id.value_end)).check(matches(withText("Mo., 19.04.21   22:52")))

        onView(withId(R.id.button_submit)).perform(click())

        navController.currentDestination?.id shouldBe R.id.traceLocationInfoFragment
    }

    @Test
    fun duplicateEventWithDifferentDatesTest() {
        launchFragmentInContainer2<TraceLocationCreateFragment>(
            fragmentArgs = TraceLocationCreateFragmentArgs(
                category = TraceLocationData.categoryEvent,
                originalItem = TraceLocationData.traceLocationDifferentDate
            ).toBundle()
        ).onFragment { fragment -> Navigation.setViewNavController(fragment.requireView(), navController) }

        onView(withId(R.id.description_input_edit)).check(matches(withText("Your Birthday Party")))
        onView(withId(R.id.place_input_edit)).check(matches(withText("at your place")))
        onView(withId(R.id.value_start)).check(matches(withText("So., 18.04.21   12:00")))
        onView(withId(R.id.value_end)).check(matches(withText("Mo., 19.04.21   22:52")))

        onView(withId(R.id.button_submit)).perform(click())

        navController.currentDestination?.id shouldBe R.id.traceLocationInfoFragment
    }

    private fun createViewModel(category: TraceLocationCategory) =
        TraceLocationCreateViewModel(
            category = category,
            traceLocationCreator = traceLocationCreator,
            dispatcherProvider = TestDispatcherProvider()
        )
}

@Module
abstract class CreateEventTestModule {
    @ContributesAndroidInjector
    abstract fun traceLocationCreateFragment(): TraceLocationCreateFragment
}
