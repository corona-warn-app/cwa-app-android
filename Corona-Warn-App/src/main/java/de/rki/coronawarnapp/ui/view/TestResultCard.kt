package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R
import kotlinx.android.synthetic.main.view_test_result_card.view.*

/**
 * The [TestResultCard] Displays the appropriate test result.
 */
class TestResultCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.view_test_result_card, this)
        context.withStyledAttributes(attrs, R.styleable.TestResultCard) {
            test_result_card_headline.text = getString(R.styleable.TestResultCard_test_result_card_headline)
            test_result_card_content.text = getString(R.styleable.TestResultCard_test_result_card_content)
            test_result_card_registered_at_text.text =
                getString(R.styleable.TestResultCard_test_result_card_registered_at_text)
            val resultIconId = getResourceId(R.styleable.TestResultCard_test_result_card_status_icon, 0)
            if (resultIconId != 0) {
                val drawable = getDrawable(context, resultIconId)
                test_result_card_status_icon.setImageDrawable(drawable)
            }
        }
    }
}
