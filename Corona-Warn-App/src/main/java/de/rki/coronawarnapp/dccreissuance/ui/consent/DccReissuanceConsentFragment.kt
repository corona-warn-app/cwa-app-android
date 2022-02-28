package de.rki.coronawarnapp.dccreissuance.ui.consent

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
            cancelButton.setOnClickListener { viewModel.navigateBack() }
            privacyInformation.setOnClickListener { viewModel.openPrivacyScreen() }
            agreeButton.setOnClickListener { viewModel.startReissuance() }

            viewModel.apply {
                stateLiveData.observe2(this@DccReissuanceConsentFragment) {
                    reissuanceGroup.isVisible = it.divisionVisible
                    dccReissuanceTitle.text = it.title
                    dccReissuanceSubtitle.text = it.subtitle
                    dccReissuanceContent.text = it.content
                    dccReissuanceLink.isVisible = !it.url.isNullOrEmpty()
                    it.url?.let { url ->
                        val text = getString(R.string.confirmed_status_faq_text)
                        dccReissuanceLink.setTextWithUrl(
                            content = text,
                            label = text,
                            url = url
                        )
                    }
                    dccReissuanceCertificateCard.certificate = it.certificate
                }

                event.observe2(this@DccReissuanceConsentFragment) { event ->
                    when (event) {
                        is DccReissuanceConsentViewModel.ReissuanceError -> {
                            agreeButton.isLoading = false
                            event.error.toErrorDialogBuilder(requireContext())
                                .setNegativeButton(R.string.errors_generic_button_positive) { _, _ -> }
                                .setPositiveButton(R.string.dcc_reissuance_try_again) { _, _ ->
                                    viewModel.startReissuance()
                                }
                                .show()
                        }
                        DccReissuanceConsentViewModel.ReissuanceInProgress -> agreeButton.isLoading = true
                        DccReissuanceConsentViewModel.ReissuanceSuccess -> {
                            agreeButton.isLoading = false
                            findNavController().navigate(
                                R.id.action_dccReissuanceConsentFragment_to_dccReissuanceSuccessFragment
                            )
                        }
                        DccReissuanceConsentViewModel.Back -> popBackStack()
                        DccReissuanceConsentViewModel.OpenPrivacyScreen -> findNavController().navigate(
                            R.id.informationPrivacyFragment
                        )
                    }
                }
            }
        }
    }
}
