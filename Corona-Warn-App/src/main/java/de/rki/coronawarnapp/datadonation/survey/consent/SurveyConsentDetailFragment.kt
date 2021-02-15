package de.rki.coronawarnapp.datadonation.survey.consent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.SurveyConsentDetailFragmentBinding
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class SurveyConsentDetailFragment : Fragment(R.layout.survey_consent_detail_fragment) {

    private val binding: SurveyConsentDetailFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }
}
