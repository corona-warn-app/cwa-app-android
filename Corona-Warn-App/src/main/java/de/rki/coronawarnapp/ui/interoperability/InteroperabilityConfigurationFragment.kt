package de.rki.coronawarnapp.ui.interoperability

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentInteroperabilityConfigurationBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel

class InteroperabilityConfigurationFragment : Fragment() {
    companion object {
        private val TAG: String? = InteroperabilityConfigurationFragment::class.simpleName
    }

    private val interoperabilityViewModel: InteroperabilityViewModel by activityViewModels()
    private var _binding: FragmentInteroperabilityConfigurationBinding? = null
    private val binding: FragmentInteroperabilityConfigurationBinding get() = _binding!!

    private var uiHelper: InteroperabilityUIHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInteroperabilityConfigurationBinding.inflate(inflater)
        binding.interopViewModel = interoperabilityViewModel
        binding.lifecycleOwner = this
        uiHelper = InteroperabilityUIHelper(interoperabilityViewModel, requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        interoperabilityViewModel.refreshInteroperability()

        binding.countryListView.allCountrySwitch.setOnCheckedChangeListener { view, checked ->
            // only if user pressed -> ignore changes done from viewmodel
            if (view.isPressed) {
                uiHelper?.handleAllCountrySwitchChanged(checked)
            }
        }

        binding.countryListView.countryList.onCountrySelectionChanged =
            { userPressed, countryCode, selected ->
                uiHelper?.handleCountrySelected(countryCode, selected, userPressed)
            }

        // register back button action
        binding.interopConfigHeader.headerButtonBack.buttonIcon.setOnClickListener {
            navBack()
        }
    }

    private fun navBack() {
        findNavController().doNavigate(
            InteroperabilityConfigurationFragmentDirections
                .actionInteropCountryConfigurationFragmentToSettingTracingFragment()
        )
    }
}
