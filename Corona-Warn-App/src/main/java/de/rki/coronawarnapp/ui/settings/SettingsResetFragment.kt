package de.rki.coronawarnapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.databinding.FragmentSettingsResetBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.util.DataRetentionHelper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The user is informed what a reset means and he can perform it.
 *
 */
class SettingsResetFragment : BaseFragment() {

    companion object {
        private val TAG: String? = SettingsResetFragment::class.simpleName
    }

    private var _binding: FragmentSettingsResetBinding? = null
    private val binding: FragmentSettingsResetBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsResetBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsResetButtonDelete.setOnClickListener {
            deleteAllAppContent()
        }
        binding.settingsResetButtonCancel.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.settingsResetHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    // TODO verify that all local data is deleted
    private fun deleteAllAppContent() {
        lifecycleScope.launch {
            try {
                val isTracingEnabled = InternalExposureNotificationClient.asyncIsEnabled()
                // only stop tracing if it is currently enabled
                if (isTracingEnabled) {
                    InternalExposureNotificationClient.asyncStop()
                    BackgroundWorkScheduler.stopWorkScheduler()
                }
            } catch (apiException: ApiException) {
                apiException.report(
                    ExceptionCategory.EXPOSURENOTIFICATION, TAG, null
                )
                Toast.makeText(
                    this@SettingsResetFragment.context,
                    "Could not stop tracing. ${apiException.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
            withContext(Dispatchers.IO) {
                deleteLocalAppContent()
            }
            navigateToOnboarding()
        }
    }

    private fun navigateToOnboarding() {
        startActivity(Intent(activity, OnboardingActivity::class.java))
        activity?.finish()
    }

    private fun deleteLocalAppContent() {
        DataRetentionHelper.clearAllLocalData(requireContext())
    }
}
