package de.rki.coronawarnapp.ui.submission.testresult.negative

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultNegativeBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.ui.dialog.RecycleBinDialogType
import de.rki.coronawarnapp.reyclebin.ui.dialog.show
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
            factory.create(navArgs.testIdentifier)
        }
    )

    private val binding: FragmentSubmissionTestResultNegativeBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            submissionTestResultButtonNegativeRemoveTest.setOnClickListener {
                showMoveToRecycleBinDialog()
            }
            binding.toolbar.setNavigationOnClickListener { popBackStack() }
            testCertificateCard.setOnClickListener { viewModel.onCertificateClicked() }
        }

        viewModel.testResult.observe2(this) { uiState ->
            val coronaTest = uiState.coronaTest
            binding.apply {
                submissionTestResultSection.setTestResultSection(coronaTest)
                if (coronaTest is FamilyCoronaTest) {
                    familyMemberName.isVisible = true
                    familyMemberName.text = coronaTest.personName
                    toolbar.title = getText(R.string.submission_test_result_headline)
                    testResultNegativeStepsRemoveTest.isVisible = false
                    testResultNegativeStepsCertificate.setEntryTitle(
                        getText(
                            R.string.submission_family_test_result_pending_steps_certificate_heading
                        )
                    )
                    testResultNegativeStepsCertificate.setEntryText(
                        getText(
                            R.string.submission_family_test_result_negative_steps_certificate_text
                        )
                    )
                    when (coronaTest.type) {
                        BaseCoronaTest.Type.PCR -> {
                            testResultNegativeStepsAdded.setEntryTitle(
                                getText(
                                    R.string.submission_family_test_result_steps_added_pcr_heading
                                )
                            )
                            testResultNegativeStepsAdded.setEntryText("")
                            testResultNegativeStepsNegativeResult.setEntryText(
                                getText(
                                    R.string.submission_test_result_negative_steps_negative_body
                                )
                            )
                        }
                        BaseCoronaTest.Type.RAPID_ANTIGEN -> {
                            testResultNegativeStepsAdded.setEntryTitle(
                                getText(
                                    R.string.submission_family_test_result_steps_added_rat_heading
                                )
                            )
                            testResultNegativeStepsAdded.setEntryText("")
                            testResultNegativeStepsNegativeResult.setEntryText(
                                getText(
                                    R.string.coronatest_negative_antigen_result_second_info_body
                                )
                            )
                        }
                    }
                }

                when (uiState.certificateState) {
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
                    findNavController().navigate(TestCertificateDetailsFragment.uri(it.containerId.qrCodeHash))
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

    private fun showMoveToRecycleBinDialog() {
        RecycleBinDialogType.RecycleTestConfirmation.show(
            fragment = this,
            positiveButtonAction = { viewModel.moveTestToRecycleBinStorage() }
        )
    }
}
