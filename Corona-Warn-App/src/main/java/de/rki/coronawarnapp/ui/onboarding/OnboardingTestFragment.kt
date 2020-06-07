package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.databinding.FragmentOnboardingTestBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.BaseFragment
import kotlinx.coroutines.launch

/**
 * This fragment informs the user about test results.
 */
class OnboardingTestFragment : BaseFragment() {
    companion object {
        private val TAG: String? = OnboardingTestFragment::class.simpleName
    }

    private var _binding: FragmentOnboardingTestBinding? = null
    private val binding: FragmentOnboardingTestBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingTestBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener {
            doNavigate(
                OnboardingTestFragmentDirections.actionOnboardingTestFragmentToOnboardingNotificationsFragment()
            )
        }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            // Deactivate tracing if user navigates back to ensure permission integrity
            lifecycleScope.launch {
                if (InternalExposureNotificationClient.asyncIsEnabled()) {
                    try {
                        InternalExposureNotificationClient.asyncStop()
                        // Reset initial activation timestamp
                        LocalData.initialTracingActivationTimestamp(0L)
                    } catch (exception: Exception) {
                        exception.report(
                            ExceptionCategory.EXPOSURENOTIFICATION,
                            OnboardingTestFragment.TAG,
                            null
                        )
                    }
                }
                (activity as OnboardingActivity).goBack()
            }
        }
    }
}
