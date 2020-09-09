package de.rki.coronawarnapp.ui.interoperability

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInteroperabilityConfigurationBinding
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel
import de.rki.coronawarnapp.util.DialogHelper
import kotlinx.coroutines.launch
import timber.log.Timber

class InteropCountryConfigurationFragment : Fragment() {
    companion object {
        private val TAG: String? = InteropCountryConfigurationFragment::class.simpleName
    }

    private val interoperabilityViewModel: InteroperabilityViewModel by activityViewModels()
    private var _binding: FragmentInteroperabilityConfigurationBinding? = null
    private val binding: FragmentInteroperabilityConfigurationBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInteroperabilityConfigurationBinding.inflate(inflater)
        binding.interopViewModel = interoperabilityViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        interoperabilityViewModel.refreshInteroperability()

        binding.countryListView.allCountrySwitch.setOnCheckedChangeListener { _, checked ->
            if (!checked) {
                showCountryDisabledDialog {
                    interoperabilityViewModel.overwriteSelectedCountries(false)
                }
            } else {
                interoperabilityViewModel.overwriteSelectedCountries(true)
            }
        }

        // register back button action
        binding.interopConfigHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }

        binding.countryListView.countryList.onCountrySelectionChanged = { countryCode, selected ->
            // TODO: Handle all country selection if all country selected disable it
            if (!selected) {
                if(interoperabilityViewModel.isAllCountriesSelected.value == true) {
                    showCountryDisabledDialog {
                        interoperabilityViewModel.setIsAllCountriesSelected(false)
                        interoperabilityViewModel.updateSelectedCountryCodes(countryCode, false)
                    }
                }else {
                    interoperabilityViewModel.setIsAllCountriesSelected(false)
                }
            } else {
                interoperabilityViewModel.updateSelectedCountryCodes(countryCode, true)
            }
        }
    }

    private fun showCountryDisabledDialog(deactivate: () -> Unit) {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.interoperability_country_disabled_dialog_headline,
            R.string.interoperability_country_disabled_dialog_body,
            R.string.interoperability_country_disabled_dialog_positive_button,
            R.string.interoperability_country_disabled_dialog_negative_button,
            false,
            {
                interoperabilityViewModel.refreshInteroperability()
            },
            deactivate
        )
        DialogHelper.showDialog(dialog)
    }
}
