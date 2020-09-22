package de.rki.coronawarnapp.ui.interoperability

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInteroperabilityConfigurationBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class InteroperabilityConfigurationFragment :
    Fragment(R.layout.fragment_interoperability_configuration) {
    companion object {
        private val TAG: String? = InteroperabilityConfigurationFragment::class.simpleName
    }

    private val interoperabilityViewModel: InteroperabilityViewModel by activityViewModels()
    private val binding: FragmentInteroperabilityConfigurationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.interoperabilityViewModel = interoperabilityViewModel
        interoperabilityViewModel.saveInteroperabilityUsed()

        binding.interoperabilityConfigurationHeader.headerButtonBack.buttonIcon.setOnClickListener {
            interoperabilityViewModel.onBackPressed()
        }

        interoperabilityViewModel.navigateBack.observe2(this) {
            if (it) {
                navBack()
            }
        }
    }

    private fun navBack() {
        (activity as? MainActivity)?.goBack()
    }
}
