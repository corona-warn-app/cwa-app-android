package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
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

    private lateinit var entryTitle: TextView
    private lateinit var entryText: TextView

    init {
        inflate(context, R.layout.include_step_entry_simple_body, this)

        context.withStyledAttributes(attrs, R.styleable.SimpleStepEntry) {
            entryTitle = findViewById(R.id.simple_step_entry_title)
            setEntryTitle(getText(R.styleable.SimpleStepEntry_simple_step_entry_title))

            entryText = findViewById(R.id.simple_step_entry_body)
            setEntryText(getText(R.styleable.SimpleStepEntry_simple_step_entry_text))
        }
    }

    fun setEntryTitle(newText: CharSequence) = entryTitle.apply { text = newText }

    fun setEntryText(newText: CharSequence) = entryText.apply {
        text = newText
        visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
    }
}
