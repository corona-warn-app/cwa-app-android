package de.rki.coronawarnapp.ui.submission.consentstatus

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R

class ConsentStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var titleTextView: TextView

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.view_consent_status, this, true)

        titleTextView = findViewById(R.id.consent_status_title)
    }

    fun updateConsent(consent: Boolean) {
        if (consent) {
            titleTextView.setText(R.string.submission_consent_view_consent_given)
        } else {
            titleTextView.setText(R.string.submission_consent_view_consent_not_given)
        }
    }
}
