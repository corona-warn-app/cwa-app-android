package de.rki.coronawarnapp.datadonation.survey.consent

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R

class SurveyConsentBlockingProgressDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.survey_consent_blocking_progress_dialog)
            .setCancelable(false)
            .create()
    }

    companion object {
        val TAG = SurveyConsentBlockingProgressDialogFragment::class.java.simpleName
    }
}
