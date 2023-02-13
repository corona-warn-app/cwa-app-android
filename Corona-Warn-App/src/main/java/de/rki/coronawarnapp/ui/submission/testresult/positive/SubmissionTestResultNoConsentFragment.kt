package de.rki.coronawarnapp.ui.submission.testresult.positive

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultPositiveNoConsentBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

/**
 * [SubmissionTestResultNoConsentFragment], the test result screen that is shown to the user if they have not provided
 * consent
 */
class SubmissionTestResultNoConsentFragment :
    Fragment(R.layout.fragment_submission_test_result_positive_no_consent),
    AutoInject {

    private val navArgs by navArgs<SubmissionTestResultNoConsentFragmentArgs>()

    @Inject lateinit var appShortcutsHelper: AppShortcutsHelper
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: FragmentSubmissionTestResultPositiveNoConsentBinding by viewBinding()
    private val viewModel: SubmissionTestResultNoConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionTestResultNoConsentViewModel.Factory
            factory.create(navArgs.testIdentifier)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onTestOpened()

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showCancelDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.uiState.observe(viewLifecycleOwner) {
            binding.submissionTestResultSection.setTestResultSection(it.coronaTest)
            if (it.coronaTest is FamilyCoronaTest) {
                binding.toolbar.title = getText(R.string.submission_test_result_headline)
                binding.familyMemberName.text = it.coronaTest.personName
            } else {
                binding.familyMemberName.isVisible = false
            }
        }

        binding.apply {
            binding.toolbar.setNavigationOnClickListener { showCancelDialog() }
            submissionTestResultPositiveNoConsentButtonAbort.setOnClickListener {
                showCancelDialog()
            }
            submissionTestResultPositiveNoConsentButtonWarnOthers.setOnClickListener {
                navigateToWarnOthers()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appShortcutsHelper.disableAllShortcuts()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun showCancelDialog() = displayDialog {
        title(R.string.submission_test_result_positive_no_consent_dialog_title)
        message(R.string.submission_test_result_positive_no_consent_dialog_message)
        positiveButton(R.string.submission_test_result_positive_no_consent_dialog_negative_button) { navigateToHome() }
        negativeButton(R.string.submission_test_result_positive_no_consent_dialog_positive_button)
    }

    private fun navigateToHome() {
        if (navArgs.comesFromDispatcherFragment) {
            findNavController().navigate(
                SubmissionTestResultNoConsentFragmentDirections
                    .actionSubmissionTestResultNoConsentFragmentToHomeFragment()
            )
        } else popBackStack()
    }

    private fun navigateToWarnOthers() {
        findNavController().navigate(
            SubmissionTestResultNoConsentFragmentDirections
                .actionSubmissionTestResultNoConsentFragmentToSubmissionResultPositiveOtherWarningNoConsentFragment(
                    testIdentifier = navArgs.testIdentifier,
                    comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                )
        )
    }
}
