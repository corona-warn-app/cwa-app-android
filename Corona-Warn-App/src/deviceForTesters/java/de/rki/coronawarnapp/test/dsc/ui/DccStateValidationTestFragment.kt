package de.rki.coronawarnapp.test.dsc.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestStateValidationDccBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class DccStateValidationTestFragment : Fragment(R.layout.fragment_test_state_validation_dcc) {

    private val viewModel: DccStateValidationTestViewModel by viewModels()
    private val binding: FragmentTestStateValidationDccBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            clearCache.setOnClickListener {
                viewModel.clearDscList()
            }
            refreshCache.setOnClickListener {
                viewModel.refresh()
            }

            searchEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.updateSearchTerm(text.toString())
            }

            resetLastCheckTime.setOnClickListener { viewModel.resetLastCheckTime() }

            checkValidityNotifications.setOnClickListener { viewModel.checkValidityNotifications() }

            refreshRevocationList.setOnClickListener { viewModel.refreshRevocationList() }
            clearRevocationList.setOnClickListener { viewModel.clearRevocationList() }

            viewModel.dscData.observe2(this@DccStateValidationTestFragment) {
                infoText.text = buildSpannedString {
                    bold { append("Last update: ") }
                    appendLine(it.lastUpdate)
                    bold { append("List item size: ") }
                    appendLine(it.listSize.toString())
                    appendLine()
                    bold { appendLine("Results") }
                    it.searchResults.forEach {
                        appendLine(it)
                    }
                }
            }

            viewModel.errorEvent.observe2(this@DccStateValidationTestFragment) {
                Toast.makeText(requireContext(), "Can't refresh List of DSCs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "DCC State Validation",
            description = "Clear or refresh Dcc Signature & Revocation lists",
            targetId = R.id.dccStateValidationTestFragment
        )
    }
}
