package de.rki.coronawarnapp.test.debugoptions.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.processphoenix.ProcessPhoenix
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestDebugoptionsBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DebugOptionsFragment : Fragment(R.layout.fragment_test_debugoptions), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: DebugOptionsFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestDebugoptionsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showDebugLogScreen.setOnClickListener {
            findNavController().navigate(R.id.debuglogFragment)
        }

        // Server environment card
        binding.environmentToggleGroup.apply {
            setOnCheckedChangeListener { group, checkedId ->
                val chip = group.findViewById<RadioButton>(checkedId)
                if (!chip.isPressed) return@setOnCheckedChangeListener
                val type = chip.text.toString()
                vm.selectEnvironmentType(type)
                displayDialog {
                    title("Restarting ↻")
                    message("Configuring $type environment. Get yourself a glass of water \uD83D\uDEB0")
                    setCancelable(false)
                }
                ProcessPhoenix.triggerRebirth(context)
            }
        }

        binding.buttonClear.setOnClickListener {
            vm.clearLaunchEnvironment()
        }

        vm.environmentState.observe(viewLifecycleOwner) { state ->
            binding.apply {

                buttonClear.isVisible = state.isOverwritten
                overwrittenWarning.isVisible = state.isOverwritten
                environmentToggleGroup.isGone = state.isOverwritten

                if (environmentToggleGroup.childCount != state.available.size) {
                    environmentToggleGroup.removeAllViews()
                    state.available.forEach { type ->
                        RadioButton(requireContext()).apply {
                            id = ViewCompat.generateViewId()
                            text = type.rawKey
                            layoutParams = RadioGroup.LayoutParams(
                                RadioGroup.LayoutParams.MATCH_PARENT,
                                RadioGroup.LayoutParams.WRAP_CONTENT
                            )
                            environmentToggleGroup.addView(this)
                        }
                    }
                }

                environmentToggleGroup.children.forEach {
                    it as RadioButton
                    it.isChecked = it.text == state.current.rawKey
                }

                environmentCdnurlDownload.text = "Download CDN" styleTo state.urlDownload
                environmentCdnurlSubmission.text = "Submission CDN" styleTo state.urlSubmission
                environmentCdnurlVerification.text = "Verification CDN" styleTo state.urlVerification
                environmentUrlDatadonation.text = "DataDonation" styleTo state.urlDataDonation
                environmentUrlLogUpload.text = "LogUpload" styleTo state.urlLogUpload
                environmentPubkeyCrowdnotifier.text = "CrowdNotifierPubKey" styleTo state.pubKeyCrowdNotifier
                environmentPubkeyAppconfig.text = "AppConfigPubKey" styleTo state.pubKeyAppConfig
                environmentDccServerUrl.text = "DccServerUrl" styleTo state.dccServerUrl
                environmentDccReissuanceServerUrl.text = "DccReissuanceServerUrl" styleTo state.dccReissuanceServerUrl
            }
        }
        vm.environmentStateChange.observe(viewLifecycleOwner) {
            showSnackBar("Environment changed to: $it\nForce stop & restart the app!")
        }
    }

    private infix fun String.styleTo(value: String) = buildSpannedString {
        val color = requireContext().getColorCompat(R.color.colorPrimary)
        append("${this@styleTo}:")
        appendLine()
        color(color) { append(value) }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Debug options",
            description = "Server environment, logging, hourly mode...",
            targetId = R.id.test_debugoptions_fragment
        )
    }
}
