package de.rki.coronawarnapp.ui.interoperability

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.databinding.FragmentInteroperabilityConfigurationBinding
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import kotlinx.coroutines.launch

class InteropCountryConfigurationFragment : Fragment() {
    companion object {
        private val TAG: String? = InteropCountryConfigurationFragment::class.simpleName
    }

    private var _binding: FragmentInteroperabilityConfigurationBinding? = null
    private val binding: FragmentInteroperabilityConfigurationBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInteroperabilityConfigurationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            binding.countryList.countryList =
                ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()
                    .supportedCountriesList
        }
    }
}
