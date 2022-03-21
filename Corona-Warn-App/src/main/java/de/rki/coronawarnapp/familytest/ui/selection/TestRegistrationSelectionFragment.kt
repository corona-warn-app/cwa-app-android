package de.rki.coronawarnapp.familytest.ui.selection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestRegistrationSelectionBinding
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class TestRegistrationSelectionFragment : Fragment(R.layout.fragment_test_registration_selection) {

    private val navArgs by navArgs<TestRegistrationSelectionFragmentArgs>()
    private val binding: FragmentTestRegistrationSelectionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
        binding.person.setOnClickListener {
            doNavigate(
                TestRegistrationSelectionFragmentDirections
                    .actionTestRegistrationSelectionFragmentToSubmissionConsentFragment(
                        coronaTestQrCode = navArgs.coronaTestQrCode
                    )
            )
        }
        binding.family.setOnClickListener {
            doNavigate(
                TestRegistrationSelectionFragmentDirections
                    .actionTestRegistrationSelectionFragmentToFamilyTestConsentFragment(
                        coronaTestQrCode = navArgs.coronaTestQrCode
                    )
            )
        }
    }
}
