package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.databinding.ViewSubmissionStatusCardFetchFailedBinding

class TestResultCardFetchFailed(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    private val inflater = LayoutInflater.from(context)
    private val binding = ViewSubmissionStatusCardFetchFailedBinding.inflate(inflater, this)

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        binding.submissionStatusCardFetchFailedButton.setOnClickListener(l)
    }
}
