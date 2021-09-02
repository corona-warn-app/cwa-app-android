package de.rki.coronawarnapp.test.booster.ui

import android.os.Bundle
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestBoosterBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class BoosterTestFragment : Fragment(R.layout.fragment_test_booster), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: BoosterTestViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentTestBoosterBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            refreshBoosterRules.setOnClickListener { viewModel.refreshBoosterRules() }
            clearBoosterRules.setOnClickListener { viewModel.clearBoosterRules() }
            runBoosterRules.setOnClickListener { viewModel.runBoosterRules() }
        }

        viewModel.persons.observe(viewLifecycleOwner) { persons ->
            binding.boosterStatus.text = buildString {
                persons.forEach { person ->
                    val boosterRule = person.data.boosterRule
                    append(
                        "%s isEligible=%s by rule=%s".format(
                            person.fullName,
                            boosterRule != null,
                            boosterRule?.identifier
                        )
                    )
                    appendLine()
                    appendLine()
                }
            }
        }

        viewModel.rules.observe(viewLifecycleOwner) { rules ->
            binding.rulesIds.text = buildSpannedString {
                append("Rules count=${rules.size}")
                appendLine()
                appendLine()
                rules.forEach { rule ->
                    append(rule.identifier)
                    appendLine()
                }
            }
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Booster Rules",
            description = "Refresh, Run Booster rules",
            targetId = R.id.boosterTestFragment
        )
    }
}
