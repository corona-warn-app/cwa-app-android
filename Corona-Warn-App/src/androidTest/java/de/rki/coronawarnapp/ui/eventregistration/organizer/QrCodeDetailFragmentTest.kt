package de.rki.coronawarnapp.ui.eventregistration.organizer

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfig
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragmentArgs
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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

    @MockK private lateinit var appConfigProvider: AppConfigProvider
    @MockK private lateinit var traceLocationRepository: TraceLocationRepository

    private val timeZone = TimeZone.getTimeZone("Europe/Berlin")

    @Before
    fun setup() {
        TimeZone.setDefault(timeZone)
        MockKAnnotations.init(this, relaxed = true)

        coEvery { traceLocationRepository.traceLocationForId(1) } returns TraceLocationData.traceLocationSameDate
        coEvery { traceLocationRepository.traceLocationForId(2) } returns TraceLocationData.traceLocationDifferentDate
        coEvery { appConfigProvider.currentConfig } returns flowOf(
            mockk<ConfigData>().apply {
                every { presenceTracing } returns mockk<PresenceTracingConfig>().apply {
                    every { qrCodeErrorCorrectionLevel } returns ErrorCorrectionLevel.M
                }
            }
        )

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

        onView(withId(R.id.title)).check(matches(withText("Jahrestreffen der deutschen SAP Anwendergruppe")))
        onView(withId(R.id.subtitle)).check(matches(withText("Hauptstr. 3, 69115 Heidelberg")))
        onView(withId(R.id.eventDate)).check(matches(withText("21.06.2021, 18:00 - 21:00 Uhr")))
    }

    @Test
    fun eventDetailForDifferentDatesTest() {
        launchFragmentInContainer2<QrCodeDetailFragment>(
            fragmentArgs = QrCodeDetailFragmentArgs(
                traceLocationId = 2
            ).toBundle()
        )
        onView(withId(R.id.title)).check(matches(withText("Event XYZ")))
        onView(withId(R.id.subtitle)).check(matches(withText("Otto-Hahn-Str. 3, 123456 Berlin")))
        onView(withId(R.id.eventDate)).check(matches(withText("18.04.2021, 12:00 - 19.04.2021, 22:52 Uhr")))
    }

    private fun createViewModel(traceLocationId: Long) =
        QrCodeDetailViewModel(
            traceLocationId = traceLocationId,
            traceLocationRepository = traceLocationRepository,
            dispatcher = TestDispatcherProvider(),
            appConfigProvider = appConfigProvider
        )
}

@Module
abstract class QrCodeDetailFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun qrCodeDetailFragment(): QrCodeDetailFragment
}
