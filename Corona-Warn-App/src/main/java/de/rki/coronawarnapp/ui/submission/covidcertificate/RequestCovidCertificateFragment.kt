package de.rki.coronawarnapp.ui.submission.covidcertificate

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.databinding.FragmentRequestCovidCertificateBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor.State
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import java.time.LocalDate
import javax.inject.Inject

class RequestCovidCertificateFragment : Fragment(R.layout.fragment_request_covid_certificate), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModelsAssisted<RequestCovidCertificateViewModel>(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as RequestCovidCertificateViewModel.Factory
            factory.create(
                testRegistrationRequest = args.testRegistrationRequest,
                coronaTestConsent = args.coronaTestConsent,
                allowTestReplacement = args.allowTestReplacement,
                personName = args.personName
            )
        }
    )
    private val binding by viewBinding<FragmentRequestCovidCertificateBinding>()
    private val args by navArgs<RequestCovidCertificateFragmentArgs>()
    private val navOptions = NavOptions.Builder().setPopUpTo(R.id.requestCovidCertificateFragment, true).build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            val isPCR = args.testRegistrationRequest is CoronaTestQRCode.PCR
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
                val dialog = state.getDialogBuilder(requireContext())
                dialog.setPositiveButton(android.R.string.ok) { _, _ -> popBackStack() }
                dialog.show()
            }
            is State.TestRegistered -> when {
                state.test.isPositive ->
                    if (state.test is FamilyCoronaTest) {
                        NavGraphDirections.actionSubmissionTestResultPendingFragment(
                            testIdentifier = state.test.identifier
                        )
                    } else {
                        NavGraphDirections.actionToSubmissionTestResultAvailableFragment(
                            testIdentifier = state.test.identifier
                        )
                    }
                        .run { findNavController().navigate(this, navOptions) }

                else ->
                    NavGraphDirections.actionSubmissionTestResultPendingFragment(
                        testIdentifier = state.test.identifier
                    )
                        .run { findNavController().navigate(this, navOptions) }
            }
        }
    }

    private fun showCloseDialog() = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.request_gc_dialog_title)
        .setMessage(R.string.request_gc_dialog_message)
        .setNegativeButton(R.string.request_gc_dialog_negative_button) { _, _ -> }
        .setPositiveButton(R.string.request_gc_dialog_positive_button) { _, _ -> popBackStack() }
        .create()
        .show()

    private fun openDatePicker() {
        // Only allow date selections in the past
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        MaterialDatePicker.Builder
            .datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
            .setTitleText(getString(R.string.request_green_certificate_birthdate_hint))
            .build()
            .apply {
                addOnPositiveButtonClickListener { timestamp ->
                    val localDate = LocalDate.ofEpochDay(timestamp)
                    binding.dateInputEdit.setText(localDate.toDayFormat())
                    viewModel.birthDateChanged(localDate)
                }
            }
            .show(childFragmentManager, "RequestGreenCertificateFragment.MaterialDatePicker")
    }
}
