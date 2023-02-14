package de.rki.coronawarnapp.ui.submission.testresult.invalid

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultInvalidBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.ui.dialog.recycleTestDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted

class SubmissionTestResultInvalidFragment : Fragment(R.layout.fragment_submission_test_result_invalid), AutoInject {

    private val navArgs by navArgs<SubmissionTestResultInvalidFragmentArgs>()

    private val viewModel: SubmissionTestResultInvalidViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionTestResultInvalidViewModel.Factory
            factory.create(navArgs.testIdentifier)
        }
    )

    private val binding: FragmentSubmissionTestResultInvalidBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            submissionTestResultButtonInvalidRemoveTest.setOnClickListener {
                showMoveToRecycleBinDialog()
            }
            toolbar.setNavigationOnClickListener { goBack() }
        }
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goBack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.testResult.observe(viewLifecycleOwner) { uiState ->
            when (uiState.coronaTest.type) {
                BaseCoronaTest.Type.PCR -> {
                    binding.apply {
                        testResultInvalidStepsPcrAdded.isVisible = true
                        testResultInvalidStepsRatAdded.isVisible = false
                        if (uiState.coronaTest is FamilyCoronaTest) {
                            familyMemberName.isVisible = true
                            familyMemberName.text = uiState.coronaTest.personName
                            testResultInvalidStepsPcrAdded.setEntryTitle(
                                getText(R.string.submission_family_test_result_steps_added_pcr_heading)
                            )
                            testResultInvalidStepsRemoveTest.isVisible = false
                            testResultInvalidStepsInvalidResult.setIsFinal(true)
                            toolbar.title = getText(R.string.submission_test_result_headline)
                        }
                    }
                }

                BaseCoronaTest.Type.RAPID_ANTIGEN -> {
                    binding.apply {
                        testResultInvalidStepsPcrAdded.isVisible = false
                        testResultInvalidStepsRatAdded.isVisible = true
                        if (uiState.coronaTest is FamilyCoronaTest) {
                            familyMemberName.isVisible = true
                            familyMemberName.text = uiState.coronaTest.personName
                            testResultInvalidStepsRatAdded.setEntryTitle(
                                getText(R.string.submission_family_test_result_steps_added_rat_heading)
                            )
                            testResultInvalidStepsRemoveTest.isVisible = false
                            testResultInvalidStepsInvalidResult.setIsFinal(true)
                            toolbar.title = getText(R.string.submission_test_result_headline)
                        }
                    }
                }
            }

            binding.submissionTestResultSection.setTestResultSection(uiState.coronaTest)
        }

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            goBack()
        }
    }

    private fun goBack() {
        if (navArgs.comesFromDispatcherFragment) {
            findNavController().navigate(
                SubmissionTestResultInvalidFragmentDirections.actionGlobalMainFragment()
            )
        } else popBackStack()
    }

    private fun showMoveToRecycleBinDialog() = recycleTestDialog { viewModel.moveTestToRecycleBinStorage() }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
