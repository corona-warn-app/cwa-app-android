package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R
import kotlinx.android.synthetic.main.view_test_result_card.view.*

/**
 * The [TestResultCard] Displays the appropriate test result.
 */
open class TestResultCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_test_result_card, this)
        val testResultCard = context.obtainStyledAttributes(attrs, R.styleable.TestResultCard)
        try {
                val headlineText = testResultCard.getString(R.styleable.TestResultCard_test_result_card_headline)
                val contentText= SpannableString(testResultCard.getString(R.styleable.TestResultCard_test_result_card_content))
                val registeredAtText = testResultCard.getString(R.styleable.TestResultCard_test_result_card_registered_at_text)
            val resultIconId = testResultCard.getResourceId(R.styleable.TestResultCard_test_result_card_status_icon, 0)
            if (resultIconId != 0) {
                val drawable = getDrawable(context, resultIconId)
                test_result_card_status_icon.setImageDrawable(drawable)
            }
            test_result_card_headline.text = headlineText
            test_result_card_content.text = contentText
            test_result_card_registered_at_text.text = registeredAtText
        } finally {
            testResultCard.recycle()
        }
    }
}



