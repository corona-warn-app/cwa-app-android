package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.passed

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.ValidationResultAdapter
import de.rki.coronawarnapp.databinding.CovidCertificateValidationPassedFragmentBinding
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class DccValidationPassedFragment : Fragment(R.layout.covid_certificate_validation_passed_fragment) {

    @Inject lateinit var factory: DccValidationPassedViewModel.Factory
    private val binding: CovidCertificateValidationPassedFragmentBinding by viewBinding()
    private val args: DccValidationPassedFragmentArgs by navArgs()
    private val viewModel: DccValidationPassedViewModel by assistedViewModel { factory.create(args.validation) }

    @Inject lateinit var validationResultAdapter: ValidationResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            covidCertificateValidationResultFragments.apply {
                setHeaderForState(
                    dccValidationState = args.validation.state,
                    ruleCount = args.validation.acceptanceRules.size
                )
                populateList(validationResultAdapter, addPadding = true)
                setNavigationOnClickListener { popBackStackTwice() }
                offsetChange()
            }

            checkAnotherCountryButton.setOnClickListener { popBackStack() }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStackTwice()
        }

        viewModel.items.observe(viewLifecycleOwner) {
            validationResultAdapter.update(it)
        }
    }

    private fun popBackStackTwice() {
        popBackStack()
        popBackStack()
    }
}
