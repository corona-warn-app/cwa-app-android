package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateOnboardingFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class CovidCertificateOnboardingFragment : Fragment(R.layout.covid_certificate_onboarding_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: CovidCertificateOnboardingViewModel by cwaViewModels { viewModelFactory }
    private val binding: CovidCertificateOnboardingFragmentBinding by viewBinding()
    private val args by navArgs<CovidCertificateOnboardingFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            if (!args.showBottomNav) {
                toolbar.apply {
                    navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                    navigationContentDescription = getString(R.string.accessibility_close)
                }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            privacyInformation.setOnClickListener { viewModel.onDataPrivacyClick() }
            buttonContinue.setOnClickListener { viewModel.onContinueClick() }
        }

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                CovidCertificateOnboardingViewModel.Event.NavigateToDataPrivacy ->
                    findNavController().navigate(
                        R.id.action_covidCertificateOnboardingFragment_to_privacyFragment
                    )
                CovidCertificateOnboardingViewModel.Event.NavigateToPersonOverview ->
                    findNavController().navigate(
                        R.id.action_covidCertificateOnboardingFragment_to_personOverviewFragment
                    )
            }
        }
    }
}
