package de.rki.coronawarnapp.ui.submission.testresult.negative

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultNegativeBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionTestResultNegativeFragment : Fragment(R.layout.fragment_submission_test_result_negative), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTestResultNegativeViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionTestResultNegativeBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            submissionTestResultButtonNegativeRemoveTest.setOnClickListener { removeTestAfterConfirmation() }
            submissionTestResultHeader.headerButtonBack.buttonIcon.setOnClickListener { popBackStack() }
        }

        viewModel.testResult.observe2(this) {
            binding.submissionTestResultSection.setTestResultSection(it.deviceUiState, it.testResultReceivedDate)
        }

        viewModel.routeToScreen.observe2(this) { navDirections ->
            navDirections?.let { doNavigate(it) } ?: popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        viewModel.onTestOpened()
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
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColorCompat(R.color.colorTextSemanticRed))
        }
    }
}
