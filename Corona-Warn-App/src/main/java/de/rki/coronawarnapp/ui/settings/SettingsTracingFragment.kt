package de.rki.coronawarnapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.databinding.FragmentSettingsTracingBinding
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.ViewBlocker
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.launch

/**
 * The user can start/stop tracing and is informed about tracing.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 * @see InternalExposureNotificationClient
 * @see InternalExposureNotificationPermissionHelper
 */
class SettingsTracingFragment : BaseFragment(),
    InternalExposureNotificationPermissionHelper.Callback {

    companion object {
        private val TAG: String? = SettingsTracingFragment::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private lateinit var binding: FragmentSettingsTracingBinding

    private lateinit var internalExposureNotificationPermissionHelper: InternalExposureNotificationPermissionHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsTracingBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        val switch = binding.settingsSwitchRowTracing.settingsSwitchRowSwitch
        val row = binding.settingsSwitchRowTracing.settingsSwitchRow
        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
        switch.setOnCheckedChangeListener { _, _ ->
            // android calls this listener also on start, so it has to be verified if the user pressed the switch
            if (switch.isPressed) {
                ViewBlocker.runAndBlockInteraction(arrayOf(row, switch)) {
                    startStopTracing()
                }
            }
        }
        row.setOnClickListener {
            ViewBlocker.runAndBlockInteraction(arrayOf(row, switch)) {
                startStopTracing()
            }
        }
        binding.settingsDetailsHeaderTracing.settingsDetailsHeaderButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        tracingViewModel.refreshIsTracingEnabled()
        settingsViewModel.refreshBluetoothEnabled()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        internalExposureNotificationPermissionHelper.onResolutionComplete(
            requestCode,
            resultCode
        )
    }

    override fun onStartPermissionGranted() {
        tracingViewModel.refreshIsTracingEnabled()
        // TODO
        BackgroundWorkScheduler.checkStart()
        Toast.makeText(requireContext(), "Tracing started successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(exception: Exception?) {
        tracingViewModel.refreshIsTracingEnabled()
        exception?.report(de.rki.coronawarnapp.exception.ExceptionCategory.EXPOSURENOTIFICATION)
        // TODO
        Toast.makeText(
            requireContext(),
            exception?.localizedMessage ?: "Unknown Error",
            Toast.LENGTH_SHORT
        ).show()
        tracingViewModel.refreshIsTracingEnabled()
    }

    private fun startStopTracing() {
        if (tracingViewModel.isTracingEnabled.value != null) {
            // if tracing is enabled when listener is activated it should be disabled
            if (tracingViewModel.isTracingEnabled.value!!) {
                lifecycleScope.launch {
                    try {
                        Toast.makeText(
                            requireContext(),
                            "Tracing stopped successfully",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        tracingViewModel.refreshIsTracingEnabled()
                    } catch (exception: Exception) {
                        exception.report(
                            de.rki.coronawarnapp.exception.ExceptionCategory.EXPOSURENOTIFICATION,
                            TAG,
                            null
                        )
                        tracingViewModel.refreshIsTracingEnabled()
                    }
                    InternalExposureNotificationClient.asyncStop()
                    tracingViewModel.refreshIsTracingEnabled()
                    BackgroundWorkScheduler.stopWorkScheduler()
                }
            } else {
                internalExposureNotificationPermissionHelper.requestPermissionToStartTracing()
            }
        }
    }
}
