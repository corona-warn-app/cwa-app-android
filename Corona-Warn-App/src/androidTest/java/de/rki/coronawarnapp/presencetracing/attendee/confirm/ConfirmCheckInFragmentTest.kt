package de.rki.coronawarnapp.presencetracing.attendee.confirm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInFragmentArgs
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import okio.ByteString.Companion.decodeBase64
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Duration
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class ConfirmCheckInFragmentTest : BaseUITest() {

    @MockK private lateinit var viewModel: ConfirmCheckInViewModel

    private val locationId = "2c69dbc182f836431f010020414be8460ce5ba890d63c770c81ca8a63fa0a462"
    private val traceLocation = TraceLocation(
        id = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "My Birthday Party",
        address = "at my place",
        startDate = Instant.ofEpochSecond(2687955L),
        endDate = Instant.ofEpochSecond(2687991L),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "CRYPTOGRAPHIC_SEED".decodeBase64()!!,
        cnPublicKey = "PUB_KEY",
        version = TraceLocation.VERSION
    )

    private val fragmentArgs = ConfirmCheckInFragmentArgs(
        locationId = locationId
    ).toBundle()

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.confirmCheckInFragment)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { viewModel.uiState } returns MutableLiveData(
            ConfirmCheckInViewModel.UiState(
                traceLocation = traceLocation,
                checkInEndOffset = Duration.ofDays(2L),
                createJournalEntry = false,
                eventInPastVisible = true,
                eventInFutureVisible = true,
                confirmButtonEnabled = true
            )
        )

    }



    @Test
    @Screenshot
    fun capture_fragment() {

        launchFragmentInContainer2<ConfirmCheckInFragment>(
            fragmentArgs = fragmentArgs,
            testNavHostController = navController
        )
        takeScreenshot<ConfirmCheckInFragment>()
    }
}
