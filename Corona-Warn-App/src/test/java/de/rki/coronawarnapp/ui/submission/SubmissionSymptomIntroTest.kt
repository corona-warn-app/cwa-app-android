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

class SubmissionSymptomIntroTest {

    private var symptomIntroduction: SubmissionSymptomIntroductionFragment = SubmissionSymptomIntroductionFragment()

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
        val constraintLayoutApply = mockk<ConstraintLayout>()
        val textViewApply = mockk<TextView>()
        val constraintLayoutReject = mockk<ConstraintLayout>()
        val constraintLayoutVerify = mockk<ConstraintLayout>()
        val textViewReject = mockk<TextView>()
        val textViewVerify = mockk<TextView>()
        val initStateLayout: Array<ConstraintLayout> =
            arrayOf(constraintLayoutReject, constraintLayoutVerify)
        val initStateTextView: Array<TextView> = arrayOf(textViewReject, textViewVerify)
        val buttonNext = mockk<Button>()

        every { buttonNext.isEnabled = any() } just Runs
        every { constraintLayoutApply.context } returns context
        every { context.getColor(R.color.colorGreenButtonNotPressed) } returns R.color.colorGreenButtonNotPressed
        every { context.getColor(R.color.colorTextGreenButtonNotPressed) } returns R.color.colorTextGreenButtonNotPressed
        every { context.getColor(R.color.colorGreenButtonPressed) } returns R.color.colorGreenButtonPressed
        every { context.getColor(R.color.colorTextGreenButtonPressed) } returns R.color.colorTextGreenButtonPressed
        every { context.getColor(R.color.colorTextGreenButtonNotPressed) } returns R.color.colorTextGreenButtonNotPressed
        every { context.getColorStateList(R.color.colorGreenButtonPressed) } returns colorStateList
        every { context.getColorStateList(R.color.colorGreenButtonNotPressed) } returns colorStateList
        every { constraintLayoutApply.backgroundTintList } returns context.getColorStateList(R.color.colorGreenButtonNotPressed)
        every {
            constraintLayoutApply.backgroundTintList =
                context.getColorStateList(R.color.colorGreenButtonNotPressed)
        } just Runs
        every { textViewApply.setTextColor(R.color.colorTextGreenButtonNotPressed) } just Runs

        symptomIntroduction.changeState(
            constraintLayoutApply,
            textViewApply,
            initStateLayout,
            initStateTextView,
            buttonNext
        )

    }
}
