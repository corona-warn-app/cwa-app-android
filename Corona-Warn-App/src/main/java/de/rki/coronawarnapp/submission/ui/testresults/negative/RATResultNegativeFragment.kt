package de.rki.coronawarnapp.submission.ui.testresults.negative

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionAntigenTestResultNegativeBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class RATResultNegativeFragment : Fragment(R.layout.fragment_submission_antigen_test_result_negative), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: RATResultNegativeViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionAntigenTestResultNegativeBinding by viewBinding()

    private val shortTime = DateTimeFormat.shortTime()

    private val deleteRatTestConfirmationDialog by lazy {
        DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_test_result_dialog_remove_test_title,
            R.string.submission_test_result_dialog_remove_test_message,
            R.string.submission_test_result_dialog_remove_test_button_positive,
            R.string.submission_test_result_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                viewModel.onDeleteTestConfirmed()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            coronatestNegativeAntigenResultButton.setOnClickListener { viewModel.onDeleteTestClicked() }
            toolbar.setNavigationOnClickListener { viewModel.onClose() }

            viewModel.testAge.observe(viewLifecycleOwner) {
                it?.let { bindView(it) }
            }

            viewModel.events.observe(viewLifecycleOwner) {
                when (it) {
                    RATResultNegativeNavigation.ShowDeleteWarning ->
                        DialogHelper.showDialog(deleteRatTestConfirmationDialog)
                    RATResultNegativeNavigation.Back -> popBackStack()
                }
            }
        }

    private fun FragmentSubmissionAntigenTestResultNegativeBinding.bindView(
        uiState: RATResultNegativeViewModel.UIState
    ) {
        resultReceivedCounter.chronometer.text = uiState.ageText

        val patientName = getString(
            R.string.submission_test_result_antigen_patient_name_placeholder,
            uiState.test.firstName ?: "",
            uiState.test.lastName ?: ""
        )

        rapidTestCardPatientInfo.text = buildSpannedString {
            bold {
                if (patientName.isNotBlank()) append(patientName)
            }
            uiState.test.dateOfBirth?.let {
                val birthDate = getString(
                    R.string.submission_test_result_antigen_patient_birth_date_placeholder,
                    it.toString(DATE_FORMAT)
                )
                if (this.isNotBlank()) append(", ")
                append(birthDate)
            }
        }

        val localTime = uiState.test.testTakenAt.toUserTimeZone()
        resultReceivedTimeAndDate.text = getString(
            R.string.coronatest_negative_antigen_result_time_date_placeholder,
            localTime.toString(DATE_FORMAT),
            localTime.toString(shortTime)
        )

        val isAnonymousTest = with(uiState.test) {
            firstName == null && lastName == null && dateOfBirth == null
        }

        val titleString = if (isAnonymousTest) {
            R.string.submission_test_result_antigen_negative_proof_title_anonymous
        } else {
            R.string.submission_test_result_antigen_negative_proof_title
        }
        negativeTestProofTitle.text = getString(titleString)

        val proofBodyString = if (isAnonymousTest) {
            R.string.submission_test_result_antigen_negative_proof_body_anonymous
        } else {
            R.string.submission_test_result_antigen_negative_proof_body
        }
        negativeTestProofBody.text = getString(proofBodyString)

        negativeTestProofAdditionalInformation.isGone = isAnonymousTest

        when (uiState.certificateState) {
            RATResultNegativeViewModel.CertificateState.NOT_REQUESTED -> {
                coronatestNegativeAntigenResultThirdInfo.setIsFinal(true)
                coronatestNegativeAntigenResultFourthInfo.isGone = true
            }
            RATResultNegativeViewModel.CertificateState.PENDING -> {
                coronatestNegativeAntigenResultThirdInfo.setIsFinal(false)
                coronatestNegativeAntigenResultFourthInfo.isGone = false
                coronatestNegativeAntigenResultFourthInfo.setEntryText(
                    getText(R.string.submission_test_result_pending_steps_test_certificate_not_available_yet_body)
                )
                coronatestNegativeAntigenResultFourthInfo.setIcon(
                    getDrawable(requireContext(), R.drawable.ic_result_pending_certificate_info)
                )
            }
            RATResultNegativeViewModel.CertificateState.AVAILABLE -> {
                coronatestNegativeAntigenResultThirdInfo.setIsFinal(false)
                coronatestNegativeAntigenResultFourthInfo.isGone = false
                coronatestNegativeAntigenResultFourthInfo.setEntryText(
                    getText(R.string.coronatest_negative_result_certificate_info_body)
                )
                coronatestNegativeAntigenResultFourthInfo.setIcon(
                    getDrawable(requireContext(), R.drawable.ic_qr_code_illustration)
                )
            }
        }
    }

    companion object {
        private const val DATE_FORMAT = "dd.MM.yyyy"
    }
}
