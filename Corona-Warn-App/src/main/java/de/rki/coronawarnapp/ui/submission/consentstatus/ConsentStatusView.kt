package de.rki.coronawarnapp.ui.submission.consentstatus

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import de.rki.coronawarnapp.R

class ConsentStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var titleTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_consent_status, this, true)

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)

        titleTextView = findViewById(R.id.consent_status_message)
    }

    var consent: Boolean = false
        set(value) {
            when (value) {
                true -> R.string.submission_consent_view_consent_given
                false -> R.string.submission_consent_view_consent_not_given
            }.let { titleTextView.setText(it) }
            field = value
        }
}

@BindingAdapter("consent")
fun ConsentStatusView.consent(newConsent: Boolean) {
    consent = newConsent
}
