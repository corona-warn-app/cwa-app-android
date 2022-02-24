package de.rki.coronawarnapp.dccreissuance.ui.consent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccReissuanceConsentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import setTextWithUrl
import javax.inject.Inject

class DccReissuanceConsentFragment : Fragment(R.layout.fragment_dcc_reissuance_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: DccReissuanceConsentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentDccReissuanceConsentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }
            cancelButton.setOnClickListener { popBackStack() }
            privacyInformation.setOnClickListener { findNavController().navigate(R.id.informationPrivacyFragment) }
            agreeButton.setOnClickListener { viewModel.startReissuance() }

            viewModel.apply {
                stateLiveData.observe2(this@DccReissuanceConsentFragment) {
                    binding.dccReissuanceTitle.text = it.title
                    binding.dccReissuanceSubtitle.text = it.subtitle
                    binding.dccReissuanceContent.text = it.content
                    it.url?.let { url ->
                        binding.dccReissuanceLink.visibility = View.VISIBLE
                        val text = getString(R.string.dcc_reissuance_faq)
                        binding.dccReissuanceLink.setTextWithUrl(
                            content = text,
                            label = text,
                            url = url
                        )
                    }
                }

                certificateLiveData.observe2(this@DccReissuanceConsentFragment) {
                    binding.dccReissuanceCertificateCard.certificate = it
                }

                event.observe2(this@DccReissuanceConsentFragment) {
                    when (it) {
                        DccReissuanceConsentViewModel.ReissuanceError -> {
                            binding.agreeButton.isLoading = false
                            // show error dialog
                        }
                        DccReissuanceConsentViewModel.ReissuanceInProgress -> binding.agreeButton.isLoading = true
                        DccReissuanceConsentViewModel.ReissuanceSuccess -> {
                            binding.agreeButton.isLoading = false
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
