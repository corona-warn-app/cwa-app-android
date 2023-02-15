package de.rki.coronawarnapp.presencetracing.attendee.checkins

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragmentArgs
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsViewModel
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.ActiveCheckInVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.PastCheckInVH
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Instant

@HiltAndroidTest
class CheckInsFragmentTest : BaseUITest() {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @MockK lateinit var viewModel: CheckInsViewModel
    private val fragmentArgs = CheckInsFragmentArgs().toBundle()

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.checkInsFragment)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    @Screenshot
    fun capture_active_checkins() {

        val checkIns = listOf(
            activeCheckInItem(
                checkInId = 1,
                checkInDescription = "Rock Konzert",
                checkInAddress = "Sponholzstraße 15, 12159 Berlin"
            ),
            activeCheckInItem(
                checkInId = 2,
                checkInDescription = "Kunstausstellung",
                checkInAddress = "Albersweilerweg 18, 12349 Berlin"
            )
        )

        every { viewModel.checkins } returns MutableLiveData(checkIns)

        launchFragmentInContainer2<CheckInsFragment>(
            fragmentArgs = fragmentArgs,
            testNavHostController = navController
        )
        takeScreenshot<CheckInsFragment>("active")
    }

    @Test
    @Screenshot
    fun capture_past_checkins() {

        val checkIns = listOf(
            pastCheckInItem(
                checkInId = 1,
                checkInDescription = "Rock Konzert",
                checkInAddress = "Sponholzstraße 15, 12159 Berlin"
            ),
            pastCheckInItem(
                checkInId = 2,
                checkInDescription = "Kunstausstellung",
                checkInAddress = "Albersweilerweg 18, 12349 Berlin"
            )
        )

        every { viewModel.checkins } returns MutableLiveData(checkIns)

        launchFragmentInContainer2<CheckInsFragment>(
            fragmentArgs = fragmentArgs,
            testNavHostController = navController
        )
        takeScreenshot<CheckInsFragment>("past")
    }

    @Test
    @Screenshot
    fun capture_empty_fragment() {

        every { viewModel.checkins } returns MutableLiveData(listOf())

        launchFragmentInContainer2<CheckInsFragment>(
            fragmentArgs = fragmentArgs,
            testNavHostController = navController
        )
        takeScreenshot<CheckInsFragment>("empty")
    }

    private fun mockCheckIn(
        checkInId: Long,
        checkInDescription: String,
        checkInAddress: String
    ) = mockk<CheckIn>().apply {
        every { id } returns checkInId
        every { description } returns checkInDescription
        every { address } returns checkInAddress
        every { checkInStart } returns Instant.parse("2021-01-01T12:30:00.000Z")
        every { checkInEnd } returns Instant.parse("2021-01-01T14:00:00.000Z")
    }

    private fun activeCheckInItem(
        checkInId: Long,
        checkInDescription: String,
        checkInAddress: String
    ) = ActiveCheckInVH.Item(
        checkin = mockCheckIn(
            checkInId = checkInId,
            checkInDescription = checkInDescription,
            checkInAddress = checkInAddress
        ),
        onCardClicked = { _, _ -> },
        onRemoveItem = {},
        onCheckout = {},
        onSwipeItem = { _, _ -> },
    )

    private fun pastCheckInItem(
        checkInId: Long,
        checkInDescription: String,
        checkInAddress: String
    ) = PastCheckInVH.Item(
        checkin = mockCheckIn(
            checkInId = checkInId,
            checkInDescription = checkInDescription,
            checkInAddress = checkInAddress
        ),
        onCardClicked = { _, _ -> },
        onRemoveItem = {},
        onSwipeItem = { _, _ -> },
    )
}
