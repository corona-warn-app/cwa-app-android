package de.rki.coronawarnapp.bugreporting.debuglog.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.format.Formatter
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import de.rki.coronawarnapp.databinding.BugreportingDebuglogFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.setGone
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import java.time.Duration
import java.time.Instant
import setTextWithUrl
import javax.inject.Inject

class DebugLogFragment : Fragment(R.layout.bugreporting_debuglog_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    @Inject lateinit var fileSharing: FileSharing

    private val vm: DebugLogViewModel by cwaViewModels { viewModelFactory }
    private val binding: BugreportingDebuglogFragmentBinding by viewBinding()

    @Suppress("ComplexMethod")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Static screen elements
        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }

            if (explanationSectionTwo.text ==
                getString(R.string.debugging_debuglog_intro_explanation_section_two)
            ) {
                explanationSectionTwo.setTextWithUrl(
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
                    setOnClickListener { vm.onSendErrorLogPress() }
                }

                toggleExportLog.apply {
                    isGone = !it.isRecording
                    isEnabled = it.currentSize > 0L && !it.isActionInProgress
                    setOnClickListener { vm.onExportLogPress() }
                }
            }
        }

        vm.events.observe2(this) {
            when (it) {
                DebugLogViewModel.Event.ShowLogDeletionRequest -> {
                    showLogDeletionRequest()
                }
                DebugLogViewModel.Event.NavigateToPrivacyFragment -> {
                    findNavController().navigate(
                        DebugLogFragmentDirections.actionDebuglogFragmentToInformationPrivacyFragment()
                    )
                }
                DebugLogViewModel.Event.NavigateToUploadFragment -> {
                    findNavController().navigate(
                        DebugLogFragmentDirections.actionDebuglogFragmentToDebugLogUploadFragment()
                    )
                }
                DebugLogViewModel.Event.NavigateToUploadHistory -> {
                    findNavController().navigate(
                        DebugLogFragmentDirections.actionDebuglogFragmentToLogUploadHistoryFragment()
                    )
                }
                is DebugLogViewModel.Event.Error -> {
                    Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_LONG).show()
                }
                DebugLogViewModel.Event.ShowLowStorageDialog -> {
                    showLowStorageError()
                }
                is DebugLogViewModel.Event.Export -> {
                    exportLog(it.snapshot)
                }
            }
        }

        vm.logUploads.observe2(this@DebugLogFragment) {
            val lastLog = it.logs.lastOrNull()?.uploadedAt

            binding.debugLogHistoryContainer.setGone(lastLog == null)

            val now = Instant.now()

            if (lastLog != null && Duration.between(lastLog, now).seconds < 3) {
                binding.scrollview.fullScroll(NestedScrollView.FOCUS_DOWN)

                binding.debugLogHistoryContainer.apply {
                    postOnAnimationDelayed(
                        {
                            isPressed = true
                            postOnAnimationDelayed({ isPressed = false }, 250)
                        },
                        250
                    )
                }
            }
        }
        binding.debugLogHistoryContainer.setOnClickListener { vm.onIdHistoryPress() }
    }

    private fun showLogDeletionRequest() = DialogHelper.showDialog(
        DialogHelper.DialogInstance(
            context = requireContext(),
            title = R.string.debugging_debuglog_stop_confirmation_title,
            message = R.string.debugging_debuglog_stop_confirmation_message,
            positiveButton = R.string.debugging_debuglog_stop_confirmation_confirmation_button,
            negativeButton = R.string.debugging_debuglog_stop_confirmation_discard_button,
            positiveButtonFunction = { vm.stopAndDeleteDebugLog() },
            isDeleteDialog = true
        )
    )

    private fun showLowStorageError() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.errors_generic_headline_short)
            setMessage(R.string.debugging_debuglog_start_low_storage_error)
            setPositiveButton(android.R.string.ok) { _, _ -> /* dismiss */ }
            setNeutralButton(R.string.menu_settings) { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }.show()
    }

    private fun exportLog(snapshot: LogSnapshotter.Snapshot) {
        val intent = fileSharing
            .getFileIntentProvider(snapshot.path, snapshot.path.name, createChooserIntent = true)
            .intent(requireActivity())
        startActivity(intent)
    }
}
