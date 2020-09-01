package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveOtherWarningBinding
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel

class SubmissionResultPositiveOtherWarningFragment : Fragment(),
    InternalExposureNotificationPermissionHelper.Callback {

    companion object {
        private val TAG: String? = SubmissionResultPositiveOtherWarningFragment::class.simpleName
    }

    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val tracingViewModel: TracingViewModel by activityViewModels()

    private var _binding: FragmentSubmissionPositiveOtherWarningBinding? = null
    private val binding: FragmentSubmissionPositiveOtherWarningBinding get() = _binding!!
    private lateinit var internalExposureNotificationPermissionHelper:
            InternalExposureNotificationPermissionHelper

    override fun onResume() {
        super.onResume()
        binding.submissionPositiveOtherPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        tracingViewModel.refreshIsTracingEnabled()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
        _binding = FragmentSubmissionPositiveOtherWarningBinding.inflate(inflater)
        binding.submissionViewModel = submissionViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        submissionViewModel.submissionState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.SUCCESS) {
                navigateToSubmissionResultPositiveEfgsConsent()
            }
        })
    }

    private fun setButtonOnClickListener() {
        binding.submissionPositiveOtherWarningHeader.headerButtonBack.buttonIcon.setOnClickListener {
            navigateToSubmissionResultFragment()
        }
        binding.submissionPositiveOtherWarningButtonNext.setOnClickListener {
            navigateToSubmissionResultPositiveEfgsConsent()
        }
    }

    private fun navigateToSubmissionResultFragment() =
        findNavController().doNavigate(
            SubmissionResultPositiveOtherWarningFragmentDirections
                .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultFragment()
        )

    /**
     * Navigate to submission done Fragment
     * @see SubmissionDoneFragment
     */
    private fun navigateToSubmissionResultPositiveEfgsConsent() =
        findNavController().doNavigate(
            SubmissionResultPositiveOtherWarningFragmentDirections
                .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultPositiveEfgsConsentFragment()
        )

    override fun onFailure(exception: Exception?) {
    }
}
