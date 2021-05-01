package de.rki.coronawarnapp.ui.eventregistration.organizer

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragmentArgs
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailViewModel
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
class QrCodeDetailFragmentTest : BaseUITest() {

    @MockK private lateinit var qrCodeGenerator: QrCodeGenerator
    @MockK private lateinit var traceLocationRepository: TraceLocationRepository

    private val timeZone = TimeZone.getTimeZone("Europe/Berlin")

    @Before
    fun setup() {
        TimeZone.setDefault(timeZone)
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(timeZone))
        MockKAnnotations.init(this, relaxed = true)

        coEvery { traceLocationRepository.traceLocationForId(1) } returns TraceLocationData.traceLocationSameDate
        coEvery { traceLocationRepository.traceLocationForId(2) } returns TraceLocationData.traceLocationDifferentDate

        setupMockViewModel(
            object : QrCodeDetailViewModel.Factory {
                override fun create(traceLocationId: Long): QrCodeDetailViewModel {
                    return createViewModel(traceLocationId)
                }
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun eventDetailForSameDatesTest() {
        launchFragmentInContainer2<QrCodeDetailFragment>(
            fragmentArgs = QrCodeDetailFragmentArgs(
                traceLocationId = 1
            ).toBundle()
        )

        onView(withId(R.id.title)).check(matches(withText("My Birthday Party")))
        onView(withId(R.id.subtitle)).check(matches(withText("at my place")))
        onView(withId(R.id.eventDate)).check(matches(withText("19.04.2021, 06:12 - 22:52 Uhr")))
    }

    @Test
    fun eventDetailForDifferentDatesTest() {
        launchFragmentInContainer2<QrCodeDetailFragment>(
            fragmentArgs = QrCodeDetailFragmentArgs(
                traceLocationId = 2
            ).toBundle()
        )
        onView(withId(R.id.title)).check(matches(withText("Your Birthday Party")))
        onView(withId(R.id.subtitle)).check(matches(withText("at your place")))
        onView(withId(R.id.eventDate)).check(matches(withText("18.04.2021, 12:00 - 19.04.2021, 22:52 Uhr")))
    }

    private fun createViewModel(traceLocationId: Long) =
        QrCodeDetailViewModel(
            traceLocationId = traceLocationId,
            qrCodeGenerator = qrCodeGenerator,
            traceLocationRepository = traceLocationRepository,
            dispatcher = TestDispatcherProvider()
        )
}

@Module
abstract class QrCodeDetailFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun qrCodeDetailFragment(): QrCodeDetailFragment
}
