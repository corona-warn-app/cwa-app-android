package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragment
import de.rki.coronawarnapp.databinding.CovidCertificateOnboardingFragmentBinding
import de.rki.coronawarnapp.qrcode.ui.DccResult
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class CovidCertificateOnboardingFragment : Fragment(R.layout.covid_certificate_onboarding_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: CovidCertificateOnboardingFragmentBinding by viewBinding()
    private val args by navArgs<CovidCertificateOnboardingFragmentArgs>()
    private val viewModel: CovidCertificateOnboardingViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as CovidCertificateOnboardingViewModel.Factory
            factory.create(
                dccType = args.dccType,
                certIdentifier = args.certIdentifier
            )
        }
    )

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
                is CovidCertificateOnboardingViewModel.Event.NavigateToDccDetailsScreen -> {
                    val uri = when (event.type) {
                        DccResult.Type.VACCINATION -> VaccinationDetailsFragment.uri(event.certIdentifier)
                        DccResult.Type.RECOVERY -> RecoveryCertificateDetailsFragment.uri(event.certIdentifier)
                        DccResult.Type.TEST -> TestCertificateDetailsFragment.uri(event.certIdentifier)
                    }
                    val navOption = NavOptions.Builder()
                        .setPopUpTo(R.id.covidCertificateOnboardingFragment, true)
                        .build()
                    findNavController().navigate(uri, navOption)
                }
            }
        }
    }

    companion object {
        fun uri(dccType: DccResult.Type, certIdentifier: String): Uri =
            "coronawarnapp://dcc.onboarding/?showBottomNav=false&dccType=$dccType&certIdentifier=$certIdentifier"
                .toUri()
    }
}
