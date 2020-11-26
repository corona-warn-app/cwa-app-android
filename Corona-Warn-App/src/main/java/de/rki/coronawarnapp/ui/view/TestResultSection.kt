package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R
import kotlinx.android.synthetic.main.view_test_result_section.view.*

/**
 * The [TestResultSection] Displays the appropriate test result.
 */
class TestResultSection @JvmOverloads
constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.view_test_result_section, this)
        context.withStyledAttributes(attrs, R.styleable.TestResultSection) {
            test_result_section_headline.text = getString(R.styleable.TestResultSection_test_result_section_headline)
            test_result_section_content.text = SpannableString(getString(R.styleable.TestResultSection_test_result_section_content))
            test_result_section_registered_at_text.text =
                getString(R.styleable.TestResultSection_test_result_section_registered_at_text)
            val resultIconId = getResourceId(R.styleable.TestResultSection_test_result_section_status_icon, 0)
            if (resultIconId != 0) {
                val drawable = getDrawable(context, resultIconId)
                test_result_section_status_icon.setImageDrawable(drawable)
            }
        }
    }
}




