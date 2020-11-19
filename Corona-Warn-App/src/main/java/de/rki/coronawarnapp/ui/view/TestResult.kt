package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R
import kotlinx.android.synthetic.main.view_step_entry.view.step_entry_icon
import kotlinx.android.synthetic.main.view_step_entry.view.step_entry_line

/**
 * The [TestResult] Displays the appropriate test result.
 */
open class TestResult @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    val body: ConstraintLayout?

    init {
        inflate(context, R.layout.view_step_entry, this)

        body = findViewById(R.id.step_entry_wrapper_children)

        context.withStyledAttributes(attrs, R.styleable.StepEntry) {
            val icon = getDrawable(R.styleable.StepEntry_step_entry_icon)
            step_entry_icon.setImageDrawable(icon)

            val isFinal = getBoolean(R.styleable.StepEntry_step_entry_final, false)
            step_entry_line.visibility = if (isFinal) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
        }
    }

    override fun addView(
        child: View?,
        index: Int,
        params: ViewGroup.LayoutParams?
    ) {
        if (body == null)
            super.addView(child, index, params)
        else
            body.addView(child, index, params)
    }
}
