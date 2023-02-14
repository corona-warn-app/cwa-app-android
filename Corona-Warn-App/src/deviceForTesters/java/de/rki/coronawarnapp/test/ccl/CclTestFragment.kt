package de.rki.coronawarnapp.test.ccl

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.text.backgroundColor
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestCclBinding
import de.rki.coronawarnapp.test.ccl.CclTestViewModel.ForceUpdateUiState.Loading
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class CclTestFragment : Fragment(R.layout.fragment_test_ccl) {

    private val viewModel: CclTestViewModel by viewModels()
    private val binding: FragmentTestCclBinding by viewBinding()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            calcDccWalletInfo.setOnClickListener { viewModel.triggerCalculation() }
            binding.forceUpdateCclConfiguration.setOnClickListener { viewModel.forceUpdateCclConfiguration() }
            clearDccWalletInfo.setOnClickListener { viewModel.clearDccWallet() }
            viewModel.dccWalletInfoList.observe(viewLifecycleOwner) { infoList ->
                dccWalletInfoList.text = buildSpannedString {
                    infoList.forEachIndexed { index, info ->
                        append("$index: ")
                        backgroundColor(Color.DKGRAY) {
                            color(Color.WHITE) {
                                append(info.toString())
                            }
                        }
                        appendLine()
                    }
                }
            }
        }

        viewModel.forceUpdateUiState.observe(viewLifecycleOwner) { uiState ->
            with(binding) {
                forceUpdateProgressBar.isVisible = uiState is Loading
                forceUpdateCclConfigurationInfo.isVisible = uiState !is Loading
            }
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "CCL Configuration & WalletInfo",
            description = "Test CCL update & WalletInfo",
            targetId = R.id.cclTestFragment
        )
    }
}
