package de.rki.coronawarnapp.test.debugoptions.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestDebugoptionsBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DebugOptionsFragment : Fragment(R.layout.fragment_test_debugoptions), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: DebugOptionsFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestDebugoptionsBinding by viewBindingLazy()

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
                vm.selectEnvironmentTytpe(chip.text.toString())
            }
        }

        vm.environmentState.observe2(this) { state ->
            binding.apply {
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

                environmentCdnurlDownload.text = "Download CDN:\n${state.urlDownload}"
                environmentCdnurlSubmission.text = "Submission CDN:\n${state.urlSubmission}"
                environmentCdnurlVerification.text = "Verification CDN:\n${state.urlVerification}"
            }
        }
        vm.environmentChangeEvent.observe2(this) {
            showSnackBar("Environment changed to: $it\nForce stop & restart the app!")
        }
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
