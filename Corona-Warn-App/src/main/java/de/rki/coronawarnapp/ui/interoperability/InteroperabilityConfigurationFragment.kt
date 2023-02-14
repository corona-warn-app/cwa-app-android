package de.rki.coronawarnapp.ui.interoperability

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInteroperabilityConfigurationBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class InteroperabilityConfigurationFragment : Fragment(R.layout.fragment_interoperability_configuration) {

    private val vm: InteroperabilityConfigurationFragmentViewModel by viewModels()
    private val binding: FragmentInteroperabilityConfigurationBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.countryList.observe(viewLifecycleOwner) {
            binding.interoperabilityConfigurationCountryList.setCountryList(it)
            if (it.isEmpty()) {
                binding.noCountriesRiskdetailsInfoview.isVisible = false
                binding.interoperabilityConfigurationCountryList.isVisible = false
                binding.noCountriesRiskdetailsInfoview.isVisible = true
            }
        }

        vm.saveInteroperabilityUsed()
        vm.navigateBack.observe(viewLifecycleOwner) {
            if (it) {
                popBackStack()
            }
        }

        binding.toolbar.setNavigationOnClickListener { popBackStack() }
        binding.noCountriesRiskdetailsInfoview.setOnClickListener {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            } else {
                Intent(Settings.ACTION_SETTINGS)
            }
            startActivity(intent)
        }
    }
}
