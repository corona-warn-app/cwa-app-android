package de.rki.coronawarnapp.ui.submission.submissiondone

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionDoneBinding
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SubmissionDoneFragment : Fragment(R.layout.fragment_submission_done), AutoInject {

    private val args by navArgs<SubmissionDoneFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionDoneViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionDoneViewModel.Factory
            factory.create(args.testType)
        }
    )

    private val binding: FragmentSubmissionDoneBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            toolbar.apply {
                navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                navigationContentDescription = getString(R.string.accessibility_close)
                setNavigationOnClickListener { viewModel.onFinishButtonClick() }
            }

            submissionDoneButtonDone.setOnClickListener {
                viewModel.onFinishButtonClick()
            }

            submissionDoneContent.submissionDoneContent.submissionDonePcrValidation.root.isVisible =
                (viewModel.testType == BaseCoronaTest.Type.RAPID_ANTIGEN)

            submissionDoneContent.submissionDoneContent.submissionDoneIllness.root.isVisible =
                (viewModel.testType == BaseCoronaTest.Type.PCR)
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                SubmissionNavigationEvents.NavigateToMainActivity -> {
                    if (args.comesFromDispatcherFragment) {
                        findNavController().navigate(
                            SubmissionDoneFragmentDirections.actionSubmissionDoneFragmentToMainFragment()
                        )
                    } else popBackStack()
                }

                else -> Unit
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDoneContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
