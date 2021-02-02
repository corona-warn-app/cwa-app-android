package de.rki.coronawarnapp.datadonation.survey.consent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.SurveyConsentFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SurveyConsentFragment : Fragment(R.layout.survey_consent_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SurveyConsentViewModel by cwaViewModels { viewModelFactory }
    private val binding: SurveyConsentFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { vm.onBackButtonPressed() }
        }

        vm.routeToScreen.observe2(this) {
            when (it) {
                is SurveyConsentNavigationEvents.NavigateBack -> activity?.onBackPressed()
            }
        }
    }
}
