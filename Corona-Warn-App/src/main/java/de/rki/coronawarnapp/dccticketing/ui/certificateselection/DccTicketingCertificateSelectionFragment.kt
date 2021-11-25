package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingCertificateSelectionBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import setTextWithUrl
import java.util.Locale
import javax.inject.Inject

class DccTicketingCertificateSelectionFragment :
    Fragment(R.layout.fragment_dcc_ticketing_certificate_selection), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<DccTicketingCertificateSelectionFragmentArgs>()
    private val binding: FragmentDccTicketingCertificateSelectionBinding by viewBinding()
    private val viewModel: DccTicketingCertificateSelectionViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccTicketingCertificateSelectionViewModel.Factory
            factory.create(args.transactionContext)
        }
    )
    private val certificatesAdapter = DccTicketingCertificateSelectionAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { returnToScreenWhereScannerWasOpened() }
            recyclerView.apply {
                adapter = certificatesAdapter
                addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            }
            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    is NavigateToConsentTwoFragment -> doNavigate(
                        DccTicketingCertificateSelectionFragmentDirections
                            .actionDccTicketingCertificateSelectionFragmentToDccTicketingConsentTwoFragment(
                                event.transactionContext,
                                event.selectedCertificateContainerId
                            )
                    )
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            val certificatesList = viewModel.getCertificates()
            certificatesAdapter.update(certificatesList)

            binding.apply {
                if (certificatesAdapter.itemCount == 0) {
                    selectionTitle.text =
                        getString(R.string.dcc_ticketing_certificate_selection_no_certificates_found_title)
                    noCertificatesFound.visibility = View.VISIBLE
                    allowedCertificates.visibility = View.INVISIBLE
                    birthDate.visibility = View.INVISIBLE
                    standardizedName.visibility = View.INVISIBLE
                    requiredCertificatesTitle.text =
                        getString(R.string.dcc_ticketing_certificate_selection_requested_certificates_text)
                    faqLink.visibility = View.VISIBLE
                    val link = when (Locale.getDefault().language) {
                        Locale.GERMAN.language -> R.string.dcc_ticketing_faq_link_german
                        else -> R.string.dcc_ticketing_faq_link_english
                    }
                    faqLink.setTextWithUrl(
                        R.string.dcc_ticketing_certificate_selection_more_information_text,
                        R.string.dcc_ticketing_faq_link_container,
                        link
                    )
                } else {
                    selectionTitle.text =
                        getString(R.string.dcc_ticketing_certificate_selection_provider_requirements_title)
                    noCertificatesFound.visibility = View.GONE
                    allowedCertificates.text = args.transactionContext.accessTokenPayload?.vc?.type?.let {
                        getRequestedCertificateTypes(
                            requestedCertificatesList = it,
                            context = requireContext(),
                            separator = ","
                        )
                    } ?: ""
                    birthDate.text = getString(R.string.dcc_ticketing_certificate_birthday)
                        .format(args.transactionContext.accessTokenPayload?.vc?.dob)
                    standardizedName.text = getFullName(
                        args.transactionContext.accessTokenPayload?.vc?.fnt,
                        args.transactionContext.accessTokenPayload?.vc?.gnt
                    )
                    requiredCertificatesTitle.text =
                        getString(R.string.dcc_ticketing_certificates_meeting_requirements_title)
                }
            }
        }
    }

    private fun returnToScreenWhereScannerWasOpened() {
        val fragmentManager = activity?.supportFragmentManager
        if (fragmentManager != null) {
            if (fragmentManager.backStackEntryCount >= 3) {
                fragmentManager.popBackStack(
                    fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 3).id,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            } else popBackStack()
        }
    }
}
