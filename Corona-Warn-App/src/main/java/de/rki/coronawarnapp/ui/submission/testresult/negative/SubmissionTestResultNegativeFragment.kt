package de.rki.coronawarnapp.ui.submission.testresult.negative

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultNegativeBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SubmissionTestResultNegativeFragment : Fragment(R.layout.fragment_submission_test_result_negative), AutoInject {

    private val navArgs by navArgs<SubmissionTestResultNegativeFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTestResultNegativeViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionTestResultNegativeViewModel.Factory
            factory.create(navArgs.testType)
        }
    )

    private val binding: FragmentSubmissionTestResultNegativeBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onTestOpened()

        binding.apply {
            submissionTestResultButtonNegativeRemoveTest.setOnClickListener { removeTestAfterConfirmation() }
            submissionTestResultHeader.headerButtonBack.buttonIcon.setOnClickListener { popBackStack() }
            testCertificateCard.setOnClickListener { viewModel.onCertificateClicked() }
        }

        viewModel.testResult.observe2(this) {
            binding.apply {
                submissionTestResultSection.setTestResultSection(it.coronaTest)

                when (it.certificateState) {
                    SubmissionTestResultNegativeViewModel.CertificateState.NOT_REQUESTED -> {
                        testResultNegativeStepsRemoveTest.setIsFinal(true)
                        testResultNegativeStepsCertificate.isGone = true
                        testCertificateCard.isGone = true
                    }
                    SubmissionTestResultNegativeViewModel.CertificateState.PENDING -> {
                        testResultNegativeStepsRemoveTest.setIsFinal(false)
                        testResultNegativeStepsCertificate.isGone = false
                        testResultNegativeStepsCertificate.setEntryText(
                            getText(
                                R.string.submission_test_result_pending_steps_test_certificate_not_available_yet_body
                            )
                        )
                        testResultNegativeStepsCertificate.setIcon(
                            getDrawable(requireContext(), R.drawable.ic_result_pending_certificate_info)
                        )
                        testCertificateCard.isGone = true
                    }
                    SubmissionTestResultNegativeViewModel.CertificateState.AVAILABLE -> {
                        testResultNegativeStepsRemoveTest.setIsFinal(false)
                        testResultNegativeStepsCertificate.isGone = false
                        testResultNegativeStepsCertificate.setEntryText(
                            getText(R.string.coronatest_negative_result_certificate_info_body)
                        )
                        testResultNegativeStepsCertificate.setIcon(
                            getDrawable(requireContext(), R.drawable.ic_qr_code_illustration)
                        )
                        testCertificateCard.isGone = false
                    }
                }
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                is SubmissionTestResultNegativeNavigation.Back -> popBackStack()
                is SubmissionTestResultNegativeNavigation.OpenTestCertificateDetails ->
                    findNavController().navigate(TestCertificateDetailsFragment.uri(it.containerId.identifier))
            }
        }

        viewModel.certificate.observe(viewLifecycleOwner) {
            binding.certificateDate.text = getString(
                R.string.test_certificate_sampled_on,
                it?.testCertificate?.sampleCollectedAt?.toUserTimeZone()?.toDayFormat()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun removeTestAfterConfirmation() {
        val removeTestDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_test_result_dialog_remove_test_title,
            R.string.submission_test_result_dialog_remove_test_message,
            R.string.submission_test_result_dialog_remove_test_button_positive,
            R.string.submission_test_result_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                viewModel.deregisterTestFromDevice()
            }
        )
        DialogHelper.showDialog(removeTestDialog).apply {
            getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(context.getColorCompat(R.color.colorTextSemanticRed))
        }
    }
}
