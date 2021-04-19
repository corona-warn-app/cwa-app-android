package de.rki.coronawarnapp.ui.submission.deletionwarning

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionDeletionWarningBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionDeletionWarningFragment : Fragment(R.layout.fragment_submission_deletion_warning), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionDeletionWarningFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionDeletionWarningBinding by viewBindingLazy()

    private val args by navArgs<SubmissionDeletionWarningFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            val testType = args.coronaTestQrCode.type
            if(testType == CoronaTest.Type.PCR) {
                headline.text = getString(R.string.submission_deletion_warning_headline_pcr_test)
                body.text = getString(R.string.submission_deletion_warning_body_pcr_test)
            } else if(testType == CoronaTest.Type.RAPID_ANTIGEN) {
                headline.text = getString(R.string.submission_deletion_warning_headline_antigen_test)
                body.text = getString(R.string.submission_deletion_warning_body_antigen_test)
            }

            cancelButton.setOnClickListener {
                doNavigate(
                    SubmissionDeletionWarningFragmentDirections
                        .actionSubmissionDeletionWarningFragmentToSubmissionConsentFragment()
                )
            }

            continueButton.setOnClickListener {
                viewModel.deleteExistingAndRegisterNewTest(args.coronaTestQrCode)
            }

            toolbar.apply {
                setNavigationOnClickListener {
                    doNavigate(
                        SubmissionDeletionWarningFragmentDirections
                            .actionSubmissionDeletionWarningFragmentToSubmissionConsentFragment()
                    )
                }
            }
        }
    }



    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
