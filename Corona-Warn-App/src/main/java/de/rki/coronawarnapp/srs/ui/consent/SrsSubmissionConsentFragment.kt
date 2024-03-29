package de.rki.coronawarnapp.srs.ui.consent

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSrsSubmissionConsentBinding
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.ui.vm.TeksSharedViewModel
import de.rki.coronawarnapp.tracing.ui.tracingConsentDialog
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

class SrsSubmissionConsentFragment : Fragment(R.layout.fragment_srs_submission_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val navArgs by navArgs<SrsSubmissionConsentFragmentArgs>()
    private val teksSharedViewModel by navGraphViewModels<TeksSharedViewModel>(R.id.srs_nav_graph)
    private val viewModel: SrsSubmissionConsentFragmentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SrsSubmissionConsentFragmentViewModel.Factory
            factory.create(
                navArgs.openTypeSelection,
                teksSharedViewModel
            )
        }
    )
    private val binding by viewBinding<FragmentSrsSubmissionConsentBinding>()
    private lateinit var keyRetrievalProgress: SubmissionBlockingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keyRetrievalProgress = SubmissionBlockingDialog(requireContext())

        binding.toolbar.setNavigationOnClickListener {
            viewModel.onConsentCancel()
        }

        binding.srsSubmissionConsentMoreInfo.setOnClickListener {
            viewModel.onDataPrivacyClick()
        }

        binding.srsSubmissionConsentAcceptButton.setOnClickListener {
            viewModel.submissionConsentAcceptButtonClicked()
        }

        binding.srsSubmissionConsentCancelButton.setOnClickListener {
            viewModel.onConsentCancel()
        }

        with(binding) {
            viewModel.timeBetweenSubmissionsInDays.observe(viewLifecycleOwner) {
                srsSectionWarnInterval.text = getString(R.string.srs_section_warn_interval_text, it.toDays())
            }
        }

        viewModel.showKeysRetrievalProgress.observe(viewLifecycleOwner) {
            Timber.i("SubmissionTestResult:showKeyRetrievalProgress:$it")
            keyRetrievalProgress.setState(it)
            binding.srsSubmissionConsentAcceptButton.isEnabled = !it
        }

        viewModel.showTracingConsentDialog.observe(viewLifecycleOwner) { onConsentResult ->
            tracingConsentDialog(
                positiveButton = { onConsentResult(true) },
                negativeButton = { onConsentResult(false) }
            )
        }

        viewModel.showPermissionRequest.observe(viewLifecycleOwner) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                SrsSubmissionConsentNavigationEvents.NavigateToDataPrivacy ->
                    findNavController().navigate(
                        SrsSubmissionConsentFragmentDirections
                            .actionSrsSubmissionConsentFragmentToSrsConsentDetailFragment()
                    )

                SrsSubmissionConsentNavigationEvents.NavigateToMainScreen ->
                    findNavController().navigate(
                        SrsSubmissionConsentFragmentDirections.actionSrsSubmissionConsentFragmentToMainFragment()
                    )

                SrsSubmissionConsentNavigationEvents.NavigateToShareCheckins ->
                    findNavController().navigate(
                        SrsSubmissionConsentFragmentDirections.actionSrsSubmissionConsentFragmentToSrsCheckinsFragment(
                            SrsSubmissionType.SRS_SELF_TEST
                        )
                    )

                SrsSubmissionConsentNavigationEvents.NavigateToShareSymptoms ->
                    findNavController().navigate(
                        SrsSubmissionConsentFragmentDirections.actionSrsSubmissionConsentFragmentToSrsSymptomsFragment(
                            submissionType = SrsSubmissionType.SRS_SELF_TEST,
                            selectedCheckIns = longArrayOf()

                        )
                    )

                SrsSubmissionConsentNavigationEvents.NavigateToTestType ->
                    findNavController().navigate(
                        SrsSubmissionConsentFragmentDirections
                            .actionSrsSubmissionConsentFragmentToSrsSubmissionTypeSelectionFragment()
                    )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.handleActivityResult(requestCode, resultCode, data)
    }
}
