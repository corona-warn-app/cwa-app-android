package de.rki.coronawarnapp.test.deltaonboarding.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestDeltaonboardingBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DeltaonboardingFragment : Fragment(R.layout.fragment_test_deltaonboarding), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: DeltaOnboardingFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestDeltaonboardingBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchContactJournalOnboarding.isChecked = vm.isContactJournalOnboardingDone()
        binding.switchDeltaOnboarding.isChecked = vm.isDeltaOnboardingDone()
        vm.changelogVersion.observe(viewLifecycleOwner) {
            binding.lastChangelogEdittext.setText(it.toString())
        }

        binding.buttonSet.setOnClickListener {
            val value = binding.lastChangelogEdittext.text.toString().toLong()
            vm.updateChangelogVersion(value)
        }

        binding.buttonClear.setOnClickListener {
            vm.clearChangelogVersion()
        }

        binding.buttonReset.setOnClickListener {
            vm.resetChangelogVersion()
        }

        binding.switchContactJournalOnboarding.setOnCheckedChangeListener { _, value ->
            vm.setContactJournalOnboardingDone(value)
        }

        binding.switchDeltaOnboarding.setOnCheckedChangeListener { _, value ->
            vm.setDeltaOboardinDone(value)
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Onboarding options",
            description = "Set delta onboarding or new release screen",
            targetId = R.id.test_deltaonboarding_fragment
        )
    }
}
