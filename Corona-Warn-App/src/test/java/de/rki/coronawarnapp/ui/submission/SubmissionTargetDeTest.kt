package de.rki.coronawarnapp.ui.submission

import android.content.Context
import android.content.res.ColorStateList
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class SubmissionTargetDeTest {

    private var targetDe: SubmissionTargetDe = SubmissionTargetDe()

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var colorStateList: ColorStateList

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun changeStateTest() {
        val constraintLayout = mockk<ConstraintLayout>()
        val textView = mockk<TextView>()
        val constraintLayout2 = mockk<ConstraintLayout>()
        val constraintLayout3 = mockk<ConstraintLayout>()
        val textView2 = mockk<TextView>()
        val textView3 = mockk<TextView>()
        val initStateLayout: Array<ConstraintLayout> =
            arrayOf(constraintLayout2, constraintLayout3)
        val initStateTextView: Array<TextView> = arrayOf(textView2, textView3)
        val buttonNext = mockk<Button>()

        every { buttonNext.isEnabled = any() } just Runs
        every { constraintLayout.context } returns context
        every { context.getColor(R.color.colorInterBtnNotPressed) } returns R.color.colorInterBtnNotPressed
        every { context.getColor(R.color.colorTextInterBtnNotPressed) } returns R.color.colorTextInterBtnNotPressed
        every { context.getColor(R.color.colorInterBtnPressed) } returns R.color.colorInterBtnPressed
        every { context.getColor(R.color.colorTextInterBtnPressed) } returns R.color.colorTextInterBtnPressed
        every { context.getColor(R.color.colorTextInterBtnNotPressed) } returns R.color.colorTextInterBtnNotPressed
        every { context.getColorStateList(R.color.colorInterBtnPressed) } returns colorStateList
        every { context.getColorStateList(R.color.colorInterBtnNotPressed) } returns colorStateList
        every { constraintLayout.backgroundTintList } returns context.getColorStateList(R.color.colorInterBtnNotPressed)
        every {
            constraintLayout.backgroundTintList =
                context.getColorStateList(R.color.colorInterBtnNotPressed)
        } just Runs
        every { textView.setTextColor(R.color.colorTextInterBtnNotPressed) } just Runs

        targetDe.changeState(
            constraintLayout,
            textView,
            initStateLayout,
            initStateTextView,
            buttonNext
        )

    }
}
