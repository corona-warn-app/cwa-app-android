package de.rki.coronawarnapp.srs.ui.consent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSrsSubmissionConsentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding

class SrsSubmissionConsentFragment : Fragment(R.layout.fragment_srs_submission_consent), AutoInject {
    private val binding by viewBinding<FragmentSrsSubmissionConsentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
