package de.rki.coronawarnapp.presencetracing.attendee.confirm

import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.attendee.TraceLocationAttendeeSettings
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInFragmentArgs
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInViewModel
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import okio.ByteString.Companion.decodeBase64
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class ConfirmCheckInFragmentTest : BaseUITest() {

    private lateinit var viewModel: ConfirmCheckInViewModel

    @MockK lateinit var verifiedTraceLocation: VerifiedTraceLocation
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var traceLocationAttendeeSettings: TraceLocationAttendeeSettings

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
        locationId = "2c69dbc182f836431f010020414be8460ce5ba890d63c770c81ca8a63fa0a462"
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

        coEvery { checkInRepository.addCheckIn(any()) } returns 1L
        every { verifiedTraceLocation.traceLocation } returns traceLocation
        every { timeStamper.nowUTC } returns Instant.parse("2021-03-04T10:30:00Z")
        every { traceLocationAttendeeSettings.createJournalEntryCheckedState } returns flowOf(true)
        coEvery { traceLocationAttendeeSettings.setCreateJournalEntryCheckedState(any()) } just runs

        viewModel = ConfirmCheckInViewModel(
            verifiedTraceLocation = verifiedTraceLocation,
            checkInRepository = checkInRepository,
            timeStamper = timeStamper,
            traceLocationAttendeeSettings = traceLocationAttendeeSettings
        )

        setupMockViewModel(
            object : ConfirmCheckInViewModel.Factory {
                override fun create(
                    verifiedTraceLocation: VerifiedTraceLocation
                ): ConfirmCheckInViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
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

@Module
abstract class ConfirmCheckInFragmentModule {
    @ContributesAndroidInjector
    abstract fun confirmCheckInFragmentTest(): ConfirmCheckInFragment
}
