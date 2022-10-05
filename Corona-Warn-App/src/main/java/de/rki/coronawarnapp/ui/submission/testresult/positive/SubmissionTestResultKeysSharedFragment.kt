package de.rki.coronawarnapp.ui.submission.testresult.positive

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultPositiveKeysSharedBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.ui.dialog.recycleTestDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

/**
 * [SubmissionTestResultKeysSharedFragment], the test result screen that is shown to the user if they have provided
 * consent.
 */
class SubmissionTestResultKeysSharedFragment :
    Fragment(R.layout.fragment_submission_test_result_positive_keys_shared),
    AutoInject {

    private val navArgs by navArgs<SubmissionTestResultKeysSharedFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTestResultKeysSharedViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionTestResultKeysSharedViewModel.Factory
            factory.create(navArgs.testIdentifier)
        }
    )

    private val binding: FragmentSubmissionTestResultPositiveKeysSharedBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onTestOpened()

        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }

        binding.deleteTest.setOnClickListener {
            viewModel.onShowDeleteTestDialog()
        }

        viewModel.uiState.observe2(this) {
            binding.apply {
                submissionTestResultSection.setTestResultSection(it.coronaTest)
                submissionDonePcrValidation.root.isVisible = it.coronaTest.type == BaseCoronaTest.Type.RAPID_ANTIGEN

                submissionDoneIllness.root.isVisible = it.coronaTest.type == BaseCoronaTest.Type.PCR

                if (it.coronaTest is FamilyCoronaTest) {
                    toolbar.title = getText(R.string.submission_test_result_headline)
                    submissionDoneText.isVisible = false
                    familyMemberName.isVisible = true
                    familyMemberName.text = it.coronaTest.personName
                }
            }
        }

        viewModel.showDeleteTestDialog.observe2(this) {
            showMoveToRecycleBinDialog()
        }

        viewModel.routeToScreen.observe2(this) {
            if (navArgs.comesFromDispatcherFragment) {
                doNavigate(
                    SubmissionTestResultKeysSharedFragmentDirections
                        .actionSubmissionTestResultKeysSharedFragmentToMainFragment()
                )
            } else popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun showMoveToRecycleBinDialog() = recycleTestDialog { viewModel.moveTestToRecycleBinStorage() }
}
