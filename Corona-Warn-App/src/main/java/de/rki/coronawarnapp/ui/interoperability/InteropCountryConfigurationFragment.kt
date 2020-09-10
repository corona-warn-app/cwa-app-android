package de.rki.coronawarnapp.ui.interoperability

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.R
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentInteroperabilityConfigurationBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel
import de.rki.coronawarnapp.util.DialogHelper

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

        binding.countryListView.allCountrySwitch.setOnCheckedChangeListener { view, checked ->
            // only if user pressed -> ignore changes done from viewmodel
            if (view.isPressed) {
                handleAllCountrySwitchChanged(checked)
            }
        }

        binding.countryListView.countryList.onCountrySelectionChanged =
            { userPressed, countryCode, selected ->
                handleCountrySelected(countryCode, selected, userPressed)
            }

        // register back button action
        binding.interopConfigHeader.headerButtonBack.buttonIcon.setOnClickListener {
            navBack()
        }
    }

    /**
     * Updates the viewmodel and saves the given country code if selected. Displays a dialog if user
     * wants to disable the country
     */
    private fun handleCountrySelected(countryCode: String, selected: Boolean, showDialog: Boolean) {
        if (!selected) {
            if (showDialog) {
                showCountryDisabledDialog {
                    interoperabilityViewModel.updateSelectedCountryCodes(countryCode, false)
                }
            }
            interoperabilityViewModel.updateSelectedCountryCodes(countryCode, false)
        } else {
            interoperabilityViewModel.updateSelectedCountryCodes(countryCode, true)
        }
    }

    /**
     * Updates the viewmodel and saves all countries if selected. Displays a dialog if user
     * wants to disable all countries
     */
    private fun handleAllCountrySwitchChanged(checked: Boolean) {
        if (!checked) {
            showCountryDisabledDialog {
                interoperabilityViewModel.overwriteSelectedCountries(false)
            }
        } else {
            interoperabilityViewModel.overwriteSelectedCountries(true)
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
            {},
            deactivate
        )
        DialogHelper.showDialog(dialog)
    }

    private fun navBack() {
        findNavController().doNavigate(
            InteropCountryConfigurationFragmentDirections
                .actionInteropCountryConfigurationFragmentToSettingTracingFragment()
        )
    }
}
