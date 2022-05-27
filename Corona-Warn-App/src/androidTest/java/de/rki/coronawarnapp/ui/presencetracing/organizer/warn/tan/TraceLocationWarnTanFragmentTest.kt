package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.tan

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionRepository
import de.rki.coronawarnapp.ui.eventregistration.organizer.TraceLocationData
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.TraceLocationWarnDuration
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import java.time.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class TraceLocationWarnTanFragmentTest : BaseUITest() {

    @MockK lateinit var organizerSubmissionRepository: OrganizerSubmissionRepository
    private val traceLocationWarnDuration = TraceLocationWarnDuration(
        TraceLocationData.traceLocationSameDate, Instant.now(), Instant.now()
    )

    private val fragmentArgs = TraceLocationWarnTanFragmentArgs(traceLocationWarnDuration).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : TraceLocationWarnTanViewModel.Factory {
                override fun create(traceLocationWarnDuration: TraceLocationWarnDuration):
                    TraceLocationWarnTanViewModel = createViewModel()
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    private fun createViewModel() = TraceLocationWarnTanViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        organizerSubmissionRepository = organizerSubmissionRepository,
        traceLocationWarnDuration = traceLocationWarnDuration
    )

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<TraceLocationWarnTanFragment>(fragmentArgs = fragmentArgs)
        Espresso.onView(ViewMatchers.withId(R.id.tan_input_edittext))
            .perform(ViewActions.click())
            .perform(ViewActions.typeText("AC9UHD65AF"))
        takeScreenshot<TraceLocationWarnTanFragment>()
    }
}

@Module
abstract class TraceLocationWarnTanFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun traceLocationWarnTanFragment(): TraceLocationWarnTanFragment
}
