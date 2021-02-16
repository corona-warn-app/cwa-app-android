package de.rki.coronawarnapp.test.deltaonboarding.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
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

        vm.changelogVersion.observe(viewLifecycleOwner) {
            binding.lastChangelogEdittext.setText(it.toString())
        }

        binding.buttonSet.setOnClickListener {
            val value = binding.lastChangelogEdittext.text.toString().toLong()
            vm.updateChangelogVersion(value)
        }

        binding.buttonClear.setOnClickListener {
            vm.resetChangelogVersion()
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Delta onboarding",
            description = "Set last Changelog Version",
            targetId = R.id.test_deltaonboarding_fragment
        )
    }
}
