package de.rki.coronawarnapp.presencetracing.attendee.checkins

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragmentArgs
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsViewModel
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.ActiveCheckInVH
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
class CheckInsFragmentTest : BaseUITest() {

    @MockK private lateinit var viewModel: CheckInsViewModel
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

        setupMockViewModel(
            object : CheckInsViewModel.Factory {
                override fun create(
                    savedState: SavedStateHandle,
                    deepLink: String?,
                    cleanHistory: Boolean
                ): CheckInsViewModel = viewModel
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

        val checkIns = listOf(
            checkInItem(
                checkInId = 1,
                checkInDescription = "Rock Konzert",
                checkInAddress = "Sponholzstraße 15, 12159 Berlin"
            ),
            checkInItem(
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
        takeScreenshot<CheckInsFragment>()
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

    private fun checkInItem(
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
}

@Module
abstract class CheckInsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun checkInsFragmentTest(): CheckInsFragment
}
