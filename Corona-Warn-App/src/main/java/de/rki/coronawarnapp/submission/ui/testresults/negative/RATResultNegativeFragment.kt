package de.rki.coronawarnapp.submission.ui.testresults.negative

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.databinding.FragmentSubmissionAntigenTestResultNegativeBinding
import de.rki.coronawarnapp.reyclebin.ui.dialog.RecycleBinDialogType
import de.rki.coronawarnapp.reyclebin.ui.dialog.show
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class RATResultNegativeFragment : Fragment(R.layout.fragment_submission_antigen_test_result_negative), AutoInject {

    private val navArgs by navArgs<RATResultNegativeFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: RATResultNegativeViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as RATResultNegativeViewModel.Factory
            factory.create(navArgs.testIdentifier)
        }
    )

    private val binding: FragmentSubmissionAntigenTestResultNegativeBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            coronatestNegativeAntigenResultButton.setOnClickListener { viewModel.onDeleteTestClicked() }
            testCertificateCard.setOnClickListener { viewModel.onCertificateClicked() }
            toolbar.setNavigationOnClickListener { viewModel.onClose() }

            viewModel.testAge.observe(viewLifecycleOwner) {
                it?.let { bindView(it) }
            }

            viewModel.certificate.observe(viewLifecycleOwner) {
                certificateDate.text = getString(
                    R.string.test_certificate_sampled_on,
                    it?.testCertificate?.sampleCollectedAt?.toUserTimeZone()?.toDayFormat()
                )
            }

            viewModel.events.observe(viewLifecycleOwner) {
                when (it) {
                    is RATResultNegativeNavigation.ShowDeleteWarning -> {
                        showMoveToRecycleBinDialog()
                    }
                    is RATResultNegativeNavigation.Back -> popBackStack()
                    is RATResultNegativeNavigation.OpenTestCertificateDetails ->
                        findNavController().navigate(TestCertificateDetailsFragment.uri(it.containerId.qrCodeHash))
                }
            }
        }

    private fun showMoveToRecycleBinDialog() {
        RecycleBinDialogType.RecycleTestConfirmation.show(
            fragment = this,
            positiveButtonAction = { viewModel.moveTestToRecycleBinStorage() }
        )
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
                    it.toDayFormat()
                )
                if (this.isNotBlank()) append(", ")
                append(birthDate)
            }
        }

        val localTime = uiState.test.testTakenAt.toUserTimeZone()
        resultReceivedTimeAndDate.text = getString(
            R.string.coronatest_negative_antigen_result_time_date_placeholder,
            localTime.toDayFormat(),
            localTime.toShortTimeFormat()
        )

        when (uiState.certificateState) {
            RATResultNegativeViewModel.CertificateState.NOT_REQUESTED -> {
                coronatestNegativeAntigenResultThirdInfo.setIsFinal(true)
                coronatestNegativeAntigenResultFourthInfo.isGone = true
                testCertificateCard.isGone = true
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
                testCertificateCard.isGone = true
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
                testCertificateCard.isGone = false
            }
        }
    }
}
