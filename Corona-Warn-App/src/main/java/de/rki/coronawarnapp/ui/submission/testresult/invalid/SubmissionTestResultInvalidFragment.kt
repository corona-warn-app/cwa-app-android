package de.rki.coronawarnapp.ui.submission.testresult.invalid

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultInvalidBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SubmissionTestResultInvalidFragment : Fragment(R.layout.fragment_submission_test_result_invalid), AutoInject {

    private val navArgs by navArgs<SubmissionTestResultInvalidFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTestResultInvalidViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionTestResultInvalidViewModel.Factory
            factory.create(navArgs.testType)
        }
    )

    private val binding: FragmentSubmissionTestResultInvalidBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onTestOpened()

        binding.apply {
            submissionTestResultButtonInvalidRemoveTest.setOnClickListener { removeTestAfterConfirmation() }
            submissionTestResultHeader.headerButtonBack.buttonIcon.setOnClickListener { popBackStack() }
        }

        binding.apply {

            when (navArgs.testType) {
                CoronaTest.Type.PCR -> {
                    testResultInvalidStepsPcrAdded.isVisible = true
                    testResultInvalidStepsRatAdded.isVisible = false
                }
                CoronaTest.Type.RAPID_ANTIGEN -> {
                    testResultInvalidStepsPcrAdded.isVisible = false
                    testResultInvalidStepsRatAdded.isVisible = true
                }
            }
        }

        viewModel.testResult.observe2(this) {
            binding.submissionTestResultSection.setTestResultSection(it.coronaTest)
        }

        viewModel.routeToScreen.observe2(this) { navDirections ->
            navDirections?.let { doNavigate(it) } ?: popBackStack()
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
