package de.rki.coronawarnapp.bugreporting.debuglog.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.format.Formatter
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.BugreportingDebuglogFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.tryHumanReadableError
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.setGone
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class DebugLogFragment : Fragment(R.layout.bugreporting_debuglog_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: DebugLogViewModel by cwaViewModels { viewModelFactory }
    private val binding: BugreportingDebuglogFragmentBinding by viewBindingLazy()

    @Suppress("ComplexMethod")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Static screen elements
        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }

            if (explanationSectionTwo.text ==
                getString(R.string.debugging_debuglog_intro_explanation_section_two)
            ) {
                explanationSectionTwo.setUrl(
                    R.string.debugging_debuglog_intro_explanation_section_two,
                    R.string.debugging_debuglog_intro_explanation_section_two_link_label,
                    R.string.debugging_debuglog_intro_explanation_section_two_faq_link
                )
            }
            debugLogPrivacyInformation.setOnClickListener { vm.onPrivacyButtonPress() }
        }

        vm.state.observe2(this) {
            binding.apply {
                debuglogActivityIndicator.setImageDrawable(
                    requireContext().getDrawableCompat(
                        if (it.isRecording) R.drawable.ic_debug_log_indicator_activated
                        else R.drawable.ic_debug_log_indicator_deactivated
                    )
                )

                debuglogStatusPrimary.text = getString(
                    when {
                        it.isRecording && it.isLowStorage -> R.string.debugging_debuglog_status_lowstorage
                        it.isRecording -> R.string.debugging_debuglog_status_recording
                        else -> R.string.debugging_debuglog_status_not_recording
                    }
                )

                debuglogStatusSecondary.text =
                    getString(
                        R.string.debugging_debuglog_status_additional_infos,
                        Formatter.formatFileSize(context, it.currentSize)
                    )

                toggleRecording.apply {
                    isActivated = it.isRecording
                    isEnabled = !it.isActionInProgress
                    text = getString(
                        if (it.isRecording) R.string.debugging_debuglog_action_stop_recording
                        else R.string.debugging_debuglog_action_start_recording
                    )
                    setOnClickListener { vm.onToggleRecording() }
                }

                toggleSendErrorLog.apply {
                    isGone = !it.isRecording
                    isEnabled = it.currentSize > 0L && !it.isActionInProgress
                    setOnClickListener { vm.onShareButtonPress() }
                }

                toggleStoreLog.apply {
                    isGone = !it.isRecording
                    isEnabled = it.currentSize > 0L && !it.isActionInProgress
                    setOnClickListener { vm.onStoreLog() }
                }
            }
        }

        vm.events.observe2(this) {
            when (it) {
                DebugLogViewModel.Event.ShowLogDeletionRequest -> {
                    showLogDeletionRequest()
                }
                DebugLogViewModel.Event.NavigateToPrivacyFragment -> {
                    doNavigate(
                        DebugLogFragmentDirections.actionDebuglogFragmentToInformationPrivacyFragment()
                    )
                }
                DebugLogViewModel.Event.NavigateToUploadFragment -> {
                    doNavigate(
                        DebugLogFragmentDirections.actionDebuglogFragmentToDebugLogUploadFragment()
                    )
                }
                DebugLogViewModel.Event.NavigateToUploadHistory -> {
                    doNavigate(
                        DebugLogFragmentDirections.actionDebuglogFragmentToLogUploadHistoryFragment()
                    )
                }
                is DebugLogViewModel.Event.Error -> {
                    Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_LONG).show()
                }
                DebugLogViewModel.Event.ShowLowStorageDialog -> {
                    showLowStorageError()
                }
                is DebugLogViewModel.Event.LocalExport -> {
                    startActivityForResult(it.request.createIntent(), it.request.id)
                }
                is DebugLogViewModel.Event.ExportResult -> {
                    showExportResult()
                }
                is DebugLogViewModel.Event.ShowLocalExportError -> {
                    showLocalExportError(it.error)
                }
            }
        }

        vm.logUploads.observe2(this@DebugLogFragment) {
            binding.debugLogHistoryContainer.setGone(it.logs.isEmpty())
        }
        binding.debugLogHistoryContainer.setOnClickListener { vm.onIdHistoryPress() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        Timber.d("onActivityResult(requestCode=$requestCode, resultCode=$resultCode, resultData=$resultData")
        vm.processSAFResult(
            requestCode,
            if (resultCode == Activity.RESULT_OK) resultData?.data else null
        )
    }

    private fun showLogDeletionRequest() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.debugging_debuglog_stop_confirmation_title)
            setMessage(R.string.debugging_debuglog_stop_confirmation_message)
            setPositiveButton(R.string.debugging_debuglog_stop_confirmation_confirmation_button) { _, _ -> vm.stopAndDeleteDebugLog() }
            setNegativeButton(R.string.debugging_debuglog_stop_confirmation_discard_button) { _, _ -> /* dismiss */ }

        }.show()
    }

    private fun showLowStorageError() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.errors_generic_headline_short)
            setMessage(R.string.debugging_debuglog_start_low_storage_error)
            setPositiveButton(android.R.string.yes) { _, _ -> /* dismiss */ }
            setNeutralButton(R.string.menu_settings) { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }.show()
    }

    private fun showExportResult() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.debugging_debuglog_localexport_title)
            setMessage(R.string.debugging_debuglog_localexport_message)
            setPositiveButton(android.R.string.yes) { _, _ -> /* dismiss */ }
        }.show()
    }

    private fun showLocalExportError(cause: Throwable) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.errors_generic_headline_short)
            setMessage(
                getString(R.string.debugging_debuglog_localexport_error_message) + "\n(" +
                    cause.tryHumanReadableError(requireContext()).description + ")"
            )
            setPositiveButton(android.R.string.yes) { _, _ -> /* dismiss */ }
            setNeutralButton(R.string.menu_settings) { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }.show()
    }
}
