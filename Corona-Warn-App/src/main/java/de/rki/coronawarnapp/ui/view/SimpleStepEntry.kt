package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R

/**
 * The [SimpleStepEntry] is a step entry with title and text as content.
 */
class SimpleStepEntry @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : StepEntry(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.include_step_entry_simple_body, this)

        context.withStyledAttributes(attrs, R.styleable.SimpleStepEntry) {
            findViewById<TextView>(R.id.simple_step_entry_textview_title).text =
                getText(R.styleable.SimpleStepEntry_simple_step_entry_title)

            findViewById<TextView>(R.id.simple_step_entry_textview_body).text =
                getText(R.styleable.SimpleStepEntry_simple_step_entry_text)
        }
    }
}
