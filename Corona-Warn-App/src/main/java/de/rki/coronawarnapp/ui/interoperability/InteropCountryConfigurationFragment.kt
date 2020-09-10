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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // register back button action
        binding.interopConfigHeader.headerButtonBack.buttonIcon.setOnClickListener {
            navBack()
        }
    }

    private fun navBack() {
        findNavController().doNavigate(
            InteropCountryConfigurationFragmentDirections
                .actionInteropCountryConfigurationFragmentToSettingTracingFragment()
        )
    }
}
