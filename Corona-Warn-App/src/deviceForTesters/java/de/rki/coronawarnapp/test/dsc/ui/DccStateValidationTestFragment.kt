package de.rki.coronawarnapp.test.dsc.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestStateValidationDccBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DccStateValidationTestFragment : Fragment(R.layout.fragment_test_state_validation_dcc), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: DccStateValidationTestViewModel by cwaViewModels { viewModelFactory }
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

            viewModel.dscData.observe(viewLifecycleOwner) {
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

            viewModel.errorEvent.observe(viewLifecycleOwner) {
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
