package de.rki.coronawarnapp.ui.submission.covidcertificate

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.databinding.FragmentRequestCovidCertificateBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor.State
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.LocalDate
import javax.inject.Inject

class RequestCovidCertificateFragment : Fragment(R.layout.fragment_request_covid_certificate), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModelsAssisted<RequestCovidCertificateViewModel>(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as RequestCovidCertificateViewModel.Factory
            factory.create(args.testRegistrationRequest, args.coronaTestConsent, args.deleteOldTest)
        }
    )
    private val binding by viewBinding<FragmentRequestCovidCertificateBinding>()
    private val args by navArgs<RequestCovidCertificateFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            val isPCR = args.testRegistrationRequest.type == CoronaTest.Type.PCR
            birthDateGroup.isVisible = isPCR
            privacyCard.pcrExtraBullet.isVisible = isPCR

            dateInputEdit.doOnTextChanged { text, _, _, _ ->
                if (text.toString().isEmpty()) viewModel.birthDateChanged(null)
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { showCloseDialog() }
            toolbar.setNavigationOnClickListener { showCloseDialog() }

            agreeButton.setOnClickListener { viewModel.onAgreeGC() }
            disagreeButton.setOnClickListener { viewModel.onDisagreeGC() }
            dateInputEdit.setOnClickListener { openDatePicker() }
            privacyInformation.setOnClickListener { findNavController().navigate(R.id.informationPrivacyFragment) }

            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    Back -> popBackStack()

                    ToDispatcherScreen ->
                        RequestCovidCertificateFragmentDirections
                            .actionRequestCovidCertificateFragmentToDispatcherFragment()
                            .run { doNavigate(this) }

                    ToHomeScreen ->
                        RequestCovidCertificateFragmentDirections.actionRequestCovidCertificateFragmentToHomeFragment()
                            .run { doNavigate(this) }
                }
            }
            viewModel.birthDate.observe(viewLifecycleOwner) { date -> agreeButton.isEnabled = !isPCR || date != null }
            viewModel.registrationState.observe(viewLifecycleOwner) { state -> handleRegistrationState(state) }
        }

    private fun handleRegistrationState(state: State) {
        val isWorking = state is State.Working
        binding.apply {
            if (isWorking) progressBar.show() else progressBar.hide()
            agreeButton.isInvisible = isWorking
            disagreeButton.isInvisible = isWorking
        }
        when (state) {
            State.Idle,
            State.Working -> {
                // Handled above
            }
            is State.Error -> {
                state.getDialogBuilder(requireContext()).apply {
                    if (state.exception is BadRequestException) {
                        setPositiveButton(R.string.submission_qr_code_scan_invalid_dialog_button_positive) { _, _ ->
                            viewModel.navigateBack()
                        }
                        setNegativeButton(R.string.submission_qr_code_scan_invalid_dialog_button_negative) { _, _ ->
                            viewModel.navigateToDispatcherScreen()
                        }
                        setOnCancelListener { viewModel.navigateToDispatcherScreen() }
                    } else {
                        setOnDismissListener { viewModel.navigateToDispatcherScreen() }
                    }
                }.show()
            }
            is State.TestRegistered -> when {
                state.test.isPositive ->
                    NavGraphDirections.actionToSubmissionTestResultAvailableFragment(testType = state.test.type)
                        .run { doNavigate(this) }

                else ->
                    NavGraphDirections.actionSubmissionTestResultPendingFragment(testType = state.test.type)
                        .run { doNavigate(this) }
            }
        }
    }

    private fun showCloseDialog() = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.request_gc_dialog_title)
        .setMessage(R.string.request_gc_dialog_message)
        .setNegativeButton(R.string.request_gc_dialog_negative_button) { _, _ -> viewModel.navigateBack() }
        .setPositiveButton(R.string.request_gc_dialog_positive_button) { _, _ -> viewModel.navigateToHomeScreen() }
        .create()
        .show()

    private fun openDatePicker() = MaterialDatePicker.Builder
        .datePicker()
        .build()
        .apply {
            addOnPositiveButtonClickListener { timestamp ->
                val localDate = LocalDate(timestamp)
                binding.dateInputEdit.setText(localDate.toDayFormat())
                viewModel.birthDateChanged(localDate)
            }
        }
        .show(childFragmentManager, "RequestGreenCertificateFragment.MaterialDatePicker")
}
