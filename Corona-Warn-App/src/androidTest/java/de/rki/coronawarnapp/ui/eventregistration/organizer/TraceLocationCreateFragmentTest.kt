package de.rki.coronawarnapp.ui.eventregistration.organizer

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
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
        runOnUiThread {
            setGraph(R.navigation.trace_location_organizer_nav_graph)
            setCurrentDestination(R.id.traceLocationCreateFragment)
        }
    }

    @Before
    fun setup() {
        TimeZone.setDefault(timeZone)
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
            themeResId = R.style.AppTheme_Main,
            fragmentArgs = TraceLocationCreateFragmentArgs(
                category = TraceLocationData.categoryEvent,
                originalItem = TraceLocationData.traceLocationSameDate,
            ).toBundle(),
            testNavHostController = navController
        )

        onView(withId(R.id.description_input_edit)).check(matches(withText("Jahrestreffen der deutschen SAP Anwendergruppe")))
        onView(withId(R.id.place_input_edit)).check(matches(withText("Hauptstr. 3, 69115 Heidelberg")))
        onView(withId(R.id.value_start)).check(matches(withText("Mo., 21.06.21   18:00")))
        onView(withId(R.id.value_end)).check(matches(withText("Mo., 21.06.21   21:00")))

        onView(withId(R.id.button_submit)).perform(click())

        navController.currentDestination?.id shouldBe R.id.traceLocationsFragment
    }

    @Test
    fun duplicateEventWithDifferentDatesTest() {
        launchFragmentInContainer2<TraceLocationCreateFragment>(
            themeResId = R.style.AppTheme_Main,
            fragmentArgs = TraceLocationCreateFragmentArgs(
                category = TraceLocationData.categoryEvent,
                originalItem = TraceLocationData.traceLocationDifferentDate
            ).toBundle(),
            testNavHostController = navController
        )

        onView(withId(R.id.description_input_edit)).check(matches(withText("Event XYZ")))
        onView(withId(R.id.place_input_edit)).check(matches(withText("Otto-Hahn-Str. 3, 123456 Berlin")))
        onView(withId(R.id.value_start)).check(matches(withText("So., 18.04.21   12:00")))
        onView(withId(R.id.value_end)).check(matches(withText("Mo., 19.04.21   22:52")))

        onView(withId(R.id.button_submit)).perform(click())

        navController.currentDestination?.id shouldBe R.id.traceLocationsFragment
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
