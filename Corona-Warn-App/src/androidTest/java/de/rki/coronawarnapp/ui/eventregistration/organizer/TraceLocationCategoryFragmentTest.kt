package de.rki.coronawarnapp.ui.eventregistration.organizer

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.TraceLocationCategoryFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.TraceLocationCategoryViewModel
import io.mockk.MockKAnnotations
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class TraceLocationCategoryFragmentTest : BaseUITest() {

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }



    @Screenshot
    @Test
    fun screenshot() {
        launchFragmentInContainer2<TraceLocationCategoryFragment>()
        takeScreenshot<TraceLocationCategoryFragment>()
    }

    private fun createViewModel() = TraceLocationCategoryViewModel()
}
