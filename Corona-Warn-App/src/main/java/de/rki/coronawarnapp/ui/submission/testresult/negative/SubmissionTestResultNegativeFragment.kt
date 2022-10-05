package de.rki.coronawarnapp.ui.submission.testresult.negative

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultNegativeBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.ui.dialog.recycleTestDialog
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeNavigation.Back
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeNavigation.OpenTestCertificateDetails
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeViewModel.CertificateState
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = navigateBackToFlowStart()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        bindViewsClicks()
        viewModel.testResult.observe2(this) { uiState -> bindTestResultViews(uiState) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.certificate.observe(viewLifecycleOwner) { certificate -> bindCertificateViews(certificate) }
    }

    private fun bindViewsClicks() = with(binding) {
        submissionTestResultButtonNegativeRemoveTest.setOnClickListener {
            showMoveToRecycleBinDialog()
        }
        toolbar.setNavigationOnClickListener { navigateBackToFlowStart() }
        testCertificateCard.setOnClickListener { viewModel.onCertificateClicked() }
    }

    private fun bindTestResultViews(uiState: SubmissionTestResultNegativeViewModel.UIState) {
        val coronaTest = uiState.coronaTest
        showTestResult(coronaTest)
        with(binding) {
            bindTestCategory(coronaTest)
            bindTestType(coronaTest)
            bindCertificateState(uiState)
        }
    }

    private fun FragmentSubmissionTestResultNegativeBinding.bindCertificateState(
        uiState: SubmissionTestResultNegativeViewModel.UIState
    ) {
        when (uiState.certificateState) {
            CertificateState.NOT_REQUESTED -> testCertificateCard.isGone = true
            CertificateState.PENDING -> testCertificateCard.isGone = true
            CertificateState.AVAILABLE -> testCertificateCard.isGone = false
        }
    }

    private fun FragmentSubmissionTestResultNegativeBinding.bindTestType(
        coronaTest: BaseCoronaTest
    ) {
        when (coronaTest.type) {
            BaseCoronaTest.Type.PCR -> {
                testResultStepsTestAdded.setEntryTitle(
                    getText(R.string.submission_family_test_result_steps_added_pcr_heading)
                )

                testResultStepsNegativeResult.setEntryText(
                    getText(R.string.submission_test_result_negative_steps_negative_body)
                )
            }

            BaseCoronaTest.Type.RAPID_ANTIGEN -> {
                when (coronaTest) {
                    is FamilyCoronaTest -> testResultStepsTestAdded.setEntryTitle(
                        getText(R.string.submission_family_test_result_steps_added_rat_heading)
                    )
                    is PersonalCoronaTest -> {
                        testResultStepsTestAdded.setEntryTitle(
                            getText(R.string.submission_test_result_steps_added_rat_heading)
                        )
                        testResultStepsTestAdded.setEntryText(
                            getText(R.string.submission_test_result_steps_added_body_rat)
                        )
                    }
                }

                testResultStepsNegativeResult.setEntryText(
                    getText(R.string.coronatest_negative_antigen_result_second_info_body)
                )
            }
        }
    }

    private fun FragmentSubmissionTestResultNegativeBinding.bindTestCategory(coronaTest: BaseCoronaTest) {
        when (coronaTest) {
            is FamilyCoronaTest -> {
                familyMemberName.isVisible = true
                familyMemberName.text = coronaTest.personName
                toolbar.title = getText(R.string.submission_test_result_headline)

                testResultStepsTestAdded.setEntryText("")
                testResultStepsRemoveTest.isVisible = false
                testResultStepsNegativeResult.setIsFinal(true)
            }
            is PersonalCoronaTest -> {
                familyMemberName.isVisible = false
                toolbar.title = getText(R.string.submission_test_result_toolbar_text)
                testResultStepsRemoveTest.setIsFinal(true)
            }
        }
    }

    private fun onNavEvent(event: SubmissionTestResultNegativeNavigation) = when (event) {
        is Back -> navigateBackToFlowStart()
        is OpenTestCertificateDetails -> findNavController().navigate(
            TestCertificateDetailsFragment.uri(event.containerId.qrCodeHash)
        )
    }

    private fun bindCertificateViews(certificate: TestCertificate?) {
        binding.testCertificateType.setText(
            when (certificate?.isPCRTestCertificate) {
                true -> R.string.test_certificate_pcr_test_type
                else -> R.string.test_certificate_rapid_test_type
            }
        )

        binding.certificateDate.text = getString(
            R.string.test_certificate_sampled_on,
            certificate?.sampleCollectedAt?.toLocalDateTimeUserTz()
                ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        )
    }

    private fun navigateBackToFlowStart() {
        if (navArgs.comesFromDispatcherFragment) {
            doNavigate(
                SubmissionTestResultNegativeFragmentDirections
                    .actionSubmissionTestResultNegativeFragmentToHomeFragment()
            )
        } else popBackStack()
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun showMoveToRecycleBinDialog() = recycleTestDialog { viewModel.moveTestToRecycleBinStorage() }

    private fun showTestResult(test: BaseCoronaTest) {
        with(binding) {
            when {
                test is RACoronaTest && test.testResult == CoronaTestResult.RAT_NEGATIVE -> {
                    submissionTestResultSection.isVisible = false

                    personalRapidTestResultNegative.isVisible = true
                    personalRapidTestResultNegative.setTestResultSection(test)
                }
                else -> {
                    personalRapidTestResultNegative.isVisible = false

                    submissionTestResultSection.isVisible = true
                    submissionTestResultSection.setTestResultSection(test)
                }
            }
        }
    }
}
