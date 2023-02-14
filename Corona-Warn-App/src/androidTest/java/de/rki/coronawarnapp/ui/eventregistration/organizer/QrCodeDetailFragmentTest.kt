package de.rki.coronawarnapp.ui.eventregistration.organizer

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragmentArgs
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailViewModel
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.util.TimeZone

@RunWith(AndroidJUnit4::class)
class QrCodeDetailFragmentTest : BaseUITest() {

    @MockK private lateinit var viewModel: QrCodeDetailViewModel

    private val fragmentArgs = QrCodeDetailFragmentArgs(traceLocationId = 1).toBundle()
    private val qrCode = CoilQrCode(ScreenshotCertificateTestData.testCertificate)
    private val timeZone = TimeZone.getTimeZone("Europe/Berlin")

    @Before
    fun setup() {
        TimeZone.setDefault(timeZone)
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : QrCodeDetailViewModel.Factory {
                override fun create(traceLocationId: Long): QrCodeDetailViewModel = viewModel
            }
        )
    }



    @Test
    fun eventDetailForSameDatesTest() {

        every { viewModel.uiState } returns MutableLiveData(
            QrCodeDetailViewModel.UiState(
                traceLocation = TraceLocationData.traceLocationSameDate,
                qrCode = qrCode
            )
        )
        launchFragmentInContainer2<QrCodeDetailFragment>(fragmentArgs = fragmentArgs)

        onView(withId(R.id.trace_location_organizer_title)).check(matches(withText("Jahrestreffen der deutschen SAP Anwendergruppe")))
        onView(withId(R.id.trace_location_organizer_subtitle)).check(matches(withText("Hauptstr. 3, 69115 Heidelberg")))
        onView(withId(R.id.eventDate)).check(matches(withText("21.06.2021, 18:00 - 21:00 Uhr")))
    }

    @Test
    fun eventDetailForDifferentDatesTest() {

        every { viewModel.uiState } returns MutableLiveData(
            QrCodeDetailViewModel.UiState(
                traceLocation = TraceLocationData.traceLocationDifferentDate,
                qrCode = qrCode
            )
        )
        launchFragmentInContainer2<QrCodeDetailFragment>(fragmentArgs = fragmentArgs)

        onView(withId(R.id.trace_location_organizer_title)).check(matches(withText("Event XYZ")))
        onView(withId(R.id.trace_location_organizer_subtitle)).check(matches(withText("Otto-Hahn-Str. 3, 123456 Berlin")))
        onView(withId(R.id.eventDate)).check(matches(withText("18.04.2021, 12:00 - 19.04.2021, 22:52 Uhr")))
    }

    @Screenshot
    @Test
    fun screenshot() {

        every { viewModel.uiState } returns MutableLiveData(
            QrCodeDetailViewModel.UiState(
                traceLocation = TraceLocationData.traceLocationTestData,
                qrCode = qrCode
            )
        )
        launchFragmentInContainer2<QrCodeDetailFragment>(fragmentArgs = fragmentArgs)

        takeScreenshot<QrCodeDetailFragment>()
    }
}
