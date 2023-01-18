package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragment
import de.rki.coronawarnapp.databinding.CovidCertificateOnboardingFragmentBinding
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import java.net.URLEncoder
import javax.inject.Inject

class CovidCertificateOnboardingFragment : Fragment(R.layout.covid_certificate_onboarding_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: CovidCertificateOnboardingFragmentBinding by viewBinding()
    private val args by navArgs<CovidCertificateOnboardingFragmentArgs>()
    private val qrcodeSharedViewModel: QrcodeSharedViewModel by navGraphViewModels(R.id.nav_graph)
    private val viewModel: CovidCertificateOnboardingViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as CovidCertificateOnboardingViewModel.Factory
            factory.create(
                dccQrCode = args.certIdentifier?.let { qrcodeSharedViewModel.dccQrCode(it) }
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.checkOnboardingStatus()

        with(binding) {
            if (!args.showBottomNav) {
                toolbar.apply {
                    navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                    navigationContentDescription = getString(R.string.accessibility_close)
                }
            } else {
                binding.root.updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.spacing_fab_padding))
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
                    val uri = when (event.containerId) {
                        is VaccinationCertificateContainerId ->
                            VaccinationDetailsFragment.uri(event.containerId.qrCodeHash)

                        is TestCertificateContainerId ->
                            TestCertificateDetailsFragment.uri(event.containerId.qrCodeHash)

                        is RecoveryCertificateContainerId ->
                            RecoveryCertificateDetailsFragment.uri(event.containerId.qrCodeHash)
                    }
                    val navOption = NavOptions.Builder()
                        .setPopUpTo(R.id.covidCertificateOnboardingFragment, true)
                        .build()
                    findNavController().navigate(uri, navOption)
                }

                is CovidCertificateOnboardingViewModel.Event.Error ->
                    showCertificateQrErrorDialog(event.throwable)

                is CovidCertificateOnboardingViewModel.Event.SkipOnboarding ->
                    if (args.showBottomNav) {
                        findNavController().navigate(
                            R.id.action_covidCertificateOnboardingFragment_to_personOverviewFragment
                        )
                    }
            }
        }
    }

    companion object {
        fun uri(
            certIdentifier: String
        ): Uri {
            val encodedCertId = URLEncoder.encode(certIdentifier, "UTF-8")
            return "cwa://dcc.onboarding/?showBottomNav=false&certIdentifier=$encodedCertId".toUri()
        }
    }
}
