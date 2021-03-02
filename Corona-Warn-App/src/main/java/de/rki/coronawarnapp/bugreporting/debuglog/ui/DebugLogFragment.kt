package de.rki.coronawarnapp.bugreporting.debuglog.ui

import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.BugreportingDebuglogFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DebugLogFragment : Fragment(R.layout.bugreporting_debuglog_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: DebugLogViewModel by cwaViewModels { viewModelFactory }
    private val binding: BugreportingDebuglogFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            if (explanationSectionTwo.text ==
                getString(R.string.debugging_debuglog_intro_explanation_section_two)
            ) {
                explanationSectionTwo.setUrl(
                    R.string.debugging_debuglog_intro_explanation_section_two,
                    R.string.debugging_debuglog_intro_explanation_section_two_link_label,
                    R.string.debugging_debuglog_intro_explanation_section_two_faq_link
                )
            }
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
                    isEnabled = !it.sharingInProgress
                    text = getString(
                        if (it.isRecording) R.string.debugging_debuglog_action_stop_recording
                        else R.string.debugging_debuglog_action_start_recording
                    )
                }

                toggleSendErrorLog.apply {
                    isGone = !it.isRecording
                    isEnabled = it.currentSize > 0L && !it.sharingInProgress
                }

                toggleStoreLog.isGone = !it.isRecording
            }
        }

        vm.routeToScreen.observe2(this) {
            when (it) {
                DebugLogNavigationEvents.NavigateToPrivacyFragment -> {
                    doNavigate(
                        DebugLogFragmentDirections.actionDebuglogFragmentToInformationPrivacyFragment()
                    )
                }
            }
        }

        vm.errorEvent.observe2(this) {
            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_LONG).show()
        }

        vm.shareEvent.observe2(this) {
            startActivity(it.get(requireActivity()))
        }

        binding.apply {
            toggleRecording.setOnClickListener { vm.toggleRecording() }
            toggleSendErrorLog.setOnClickListener { vm.shareRecording() }
            debugLogHistoryContainer.setOnClickListener { vm.onIdHistoryPress() }
            debugLogPrivacyInformation.setOnClickListener { vm.onPrivacyButtonPress() }
            toolbar.setNavigationOnClickListener { popBackStack() }
        }
    }
}
