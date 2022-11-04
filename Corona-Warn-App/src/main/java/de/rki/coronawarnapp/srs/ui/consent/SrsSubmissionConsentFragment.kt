package de.rki.coronawarnapp.srs.ui.consent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSrsSubmissionConsentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SrsSubmissionConsentFragment : Fragment(R.layout.fragment_srs_submission_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val navArgs by navArgs<SrsSubmissionConsentFragmentArgs>()
    private val viewModel: SrsSubmissionConsentFragmentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SrsSubmissionConsentFragmentViewModel.Factory
            factory.create(navArgs.srsSubmissionType, navArgs.unregisteredTest)
        }
    )
    private val binding by viewBinding<FragmentSrsSubmissionConsentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            viewModel.onConsentCancel()
        }

        binding.srsSubmissionConsentMoreInfo.setOnClickListener {
            viewModel.onDataPrivacyClick()
        }

        binding.srsSubmissionConsentButtonNext.setOnClickListener {
            viewModel.proceed()
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                SrsSubmissionConsentNavigationEvents.NavigateToDataPrivacy ->
                    findNavController().navigate(R.id.informationPrivacyFragment)
                SrsSubmissionConsentNavigationEvents.NavigateToMainScreen ->
                    findNavController().navigate(
                        SrsSubmissionConsentFragmentDirections.actionSrsSubmissionConsentFragmentToMainFragment()
                    )
                SrsSubmissionConsentNavigationEvents.NavigateToShareCheckins ->
                    findNavController().navigate(
                        SrsSubmissionConsentFragmentDirections.actionSrsSubmissionConsentFragmentToSrsCheckinsFragment()
                    )
                SrsSubmissionConsentNavigationEvents.NavigateToShareSymptoms ->
                    findNavController().navigate(
                        SrsSubmissionConsentFragmentDirections.actionSrsSubmissionConsentFragmentToSrsSymptomsFragment()
                    )
                SrsSubmissionConsentNavigationEvents.NavigateToTestType ->
                    findNavController().navigate(
                        SrsSubmissionConsentFragmentDirections
                            .actionSrsSubmissionConsentFragmentToSrsSubmissionTypeSelectionFragment()
                    )
            }
        }
    }
}
