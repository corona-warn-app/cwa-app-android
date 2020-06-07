package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingTracingBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.util.DialogHelper
import kotlinx.android.synthetic.main.fragment_onboarding_tracing.*
import kotlinx.coroutines.launch

/**
 * This fragment ask the user if he wants to enable tracing.
 *
 * @see InternalExposureNotificationPermissionHelper
 * @see AlertDialog
 */
class OnboardingTracingFragment : BaseFragment(),
    InternalExposureNotificationPermissionHelper.Callback {

    companion object {
        private val TAG: String? = OnboardingTracingFragment::class.simpleName
    }

    private lateinit var internalExposureNotificationPermissionHelper: InternalExposureNotificationPermissionHelper
    private var _binding: FragmentOnboardingTracingBinding? = null
    private val binding: FragmentOnboardingTracingBinding get() = _binding!!

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        internalExposureNotificationPermissionHelper.onResolutionComplete(
            requestCode,
            resultCode
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingTracingBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
<<<<<<< HEAD
        onboarding_tracing_container.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onStart() {
        super.onStart()
        onboarding_tracing_container.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onResume() {
        super.onResume()
        onboarding_tracing_container.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
=======
        resetTracing()
>>>>>>> dev
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener {
            internalExposureNotificationPermissionHelper.requestPermissionToStartTracing()
        }
        binding.onboardingButtonDisable.setOnClickListener {
            showCancelDialog()
        }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            (activity as OnboardingActivity).goBack()
        }
    }

    override fun onStartPermissionGranted() {
        navigate()
    }

    override fun onFailure(exception: Exception?) {
        navigate()
    }

    private fun showCancelDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.onboarding_tracing_dialog_headline,
            R.string.onboarding_tracing_dialog_body,
            R.string.onboarding_tracing_dialog_button_positive,
            R.string.onboarding_tracing_dialog_button_negative,
            true,
            {
                navigate()
            })
        DialogHelper.showDialog(dialog)
    }

    private fun navigate() {
        doNavigate(
            OnboardingTracingFragmentDirections.actionOnboardingTracingFragmentToOnboardingTestFragment()
        )
    }

    private fun resetTracing() {
        // Reset tracing state in onboarding
        lifecycleScope.launch {
            if (InternalExposureNotificationClient.asyncIsEnabled()) {
                try {
                    InternalExposureNotificationClient.asyncStop()
                    // Reset initial activation timestamp
                    LocalData.initialTracingActivationTimestamp(0L)
                } catch (exception: Exception) {
                    exception.report(
                        ExceptionCategory.EXPOSURENOTIFICATION,
                        OnboardingTracingFragment.TAG,
                        null
                    )
                }
            }
        }
    }
}
