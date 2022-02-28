package de.rki.coronawarnapp.dccreissuance.ui.consent

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccReissuanceConsentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import setTextWithUrl
import javax.inject.Inject

class DccReissuanceConsentFragment : Fragment(R.layout.fragment_dcc_reissuance_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: FragmentDccReissuanceConsentBinding by viewBinding()
    private val args by navArgs<DccReissuanceConsentFragmentArgs>()
    private val viewModel: DccReissuanceConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccReissuanceConsentViewModel.Factory
            factory.create(
                personIdentifierCode = args.personIdentifierCode,
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }
            cancelButton.setOnClickListener { popBackStack() }
            privacyInformation.setOnClickListener { findNavController().navigate(R.id.informationPrivacyFragment) }
            agreeButton.setOnClickListener { viewModel.startReissuance() }

            viewModel.apply {
                stateLiveData.observe2(this@DccReissuanceConsentFragment) {
                    dccReissuanceTitle.isVisible = it.divisionVisible
                    dccReissuanceSubtitle.isVisible = it.divisionVisible
                    dccReissuanceContent.isVisible = it.divisionVisible
                    dccReissuanceLink.isVisible = it.divisionVisible
                    dccReissuanceTitle.text = it.title
                    dccReissuanceSubtitle.text = it.subtitle
                    dccReissuanceContent.text = it.content
                    dccReissuanceLink.isVisible = !it.url.isNullOrEmpty()
                    it.url?.let { url ->
                        val text = getString(R.string.dcc_reissuance_faq)
                        dccReissuanceLink.setTextWithUrl(
                            content = text,
                            label = text,
                            url = url
                        )
                    }
                    dccReissuanceCertificateCard.certificate = it.certificate
                }

                certificateLiveData.observe2(this@DccReissuanceConsentFragment) {

                }

                event.observe2(this@DccReissuanceConsentFragment) {
                    when (it) {
                        DccReissuanceConsentViewModel.ReissuanceError -> {
                            agreeButton.isLoading = false
                            // show error dialog
                        }
                        DccReissuanceConsentViewModel.ReissuanceInProgress -> agreeButton.isLoading = true
                        DccReissuanceConsentViewModel.ReissuanceSuccess -> {
                            agreeButton.isLoading = false
                            findNavController().navigate(
                                R.id.action_dccReissuanceConsentFragment_to_dccReissuanceSuccessFragment
                            )
                        }
                    }
                }
            }
        }
    }
}
