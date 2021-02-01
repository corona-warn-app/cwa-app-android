package de.rki.coronawarnapp.datadonation.survey.ui.onboarding

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R

class SurveyConsentFragment : Fragment() {

    companion object {
        fun newInstance() = SurveyConsentFragment()
    }

    private lateinit var viewModel: SurveyConsentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.survey_consent_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SurveyConsentViewModel::class.java)
        // TODO: Use the ViewModel
    }
}
