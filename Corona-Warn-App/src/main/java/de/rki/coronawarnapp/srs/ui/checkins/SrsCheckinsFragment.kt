package de.rki.coronawarnapp.srs.ui.checkins

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CheckInsConsentFragmentBinding
import de.rki.coronawarnapp.srs.ui.dialogs.showCloseDialog
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.CheckInsConsentAdapter
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.SelectableCheckInVH
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

class SrsCheckinsFragment : Fragment(R.layout.check_ins_consent_fragment), AutoInject {

    private val binding: CheckInsConsentFragmentBinding by viewBinding()

    private val navArgs by navArgs<SrsCheckinsFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SrsCheckinsFragmentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, savedState ->
            factory as SrsCheckinsFragmentViewModel.Factory
            factory.create(
                savedState = savedState,
                submissionType = navArgs.submissionType
            )
        }
    )

    private val adapter = CheckInsConsentAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onCloseClick()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        with(binding) {
            checkInsRecycler.adapter = adapter
            toolbar.setNavigationOnClickListener { viewModel.onCloseClick() }
            skipButton.setOnClickListener { viewModel.onSkipClick() }
            continueButton.setOnClickListener { viewModel.onNextClick() }
        }

        viewModel.checkIns.observe(viewLifecycleOwner) {
            adapter.update(it)
            binding.continueButton.isEnabled = it.any { item ->
                item is SelectableCheckInVH.Item && item.checkIn.hasSubmissionConsent
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                SrsCheckinsNavigation.ShowCloseDialog -> showCloseDialog { viewModel.goHome() }
                SrsCheckinsNavigation.ShowSkipDialog -> showSkipDialog()
                SrsCheckinsNavigation.GoToHome -> findNavController().navigate(
                    SrsCheckinsFragmentDirections.actionSrsCheckInsFragmentToMainFragment()
                )

                is SrsCheckinsNavigation.GoToSymptomSubmission -> findNavController().navigate(
                    SrsCheckinsFragmentDirections.actionSrsCheckinsFragmentToSrsSymptomsFragment(
                        submissionType = it.submissionType,
                        selectedCheckIns = it.selectedCheckIns
                    )
                )
            }
        }
    }

    private fun showSkipDialog() = displayDialog {
        title(R.string.trace_location_attendee_consent_dialog_title)
        message(R.string.trace_location_attendee_consent_dialog_message)
        positiveButton(R.string.trace_location_attendee_consent_dialog_positive_button) {
            Timber.d("showSkipDialog:Stay on CheckInsConsentFragment")
        }
        negativeButton(R.string.trace_location_attendee_consent_dialog_negative_button) {
            viewModel.doNotShareCheckIns()
        }
    }
}
