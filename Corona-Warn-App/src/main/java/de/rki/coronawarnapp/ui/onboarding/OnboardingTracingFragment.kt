package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingTracingBinding
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.util.DialogHelper

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
    private lateinit var binding: FragmentOnboardingTracingBinding

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
        binding = FragmentOnboardingTracingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
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
}
