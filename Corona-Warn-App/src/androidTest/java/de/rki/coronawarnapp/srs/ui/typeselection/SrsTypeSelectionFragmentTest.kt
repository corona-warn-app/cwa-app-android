package de.rki.coronawarnapp.srs.ui.typeselection

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SrsTypeSelectionFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SrsTypeSelectionFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel.apply {
            every { navigation } returns SingleLiveEvent()
            every { types } returns MutableLiveData(
                SrsSubmissionType.values().filterNot { it == SrsSubmissionType.SRS_SELF_TEST }.toList().map {
                    SrsTypeSelectionItem(submissionType = it)
                }
            )
        }
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<SrsTypeSelectionFragment>()
        takeScreenshot<SrsTypeSelectionFragment>()
    }
}
