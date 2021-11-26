package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingCertificateSelectionBinding
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
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

    private val binding: FragmentDccTicketingCertificateSelectionBinding by viewBinding()
    private val dccTicketingSharedViewModel:
        DccTicketingSharedViewModel by navGraphViewModels(R.id.dcc_ticketing_nav_graph)
    private val viewModel: DccTicketingCertificateSelectionViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccTicketingCertificateSelectionViewModel.Factory
            factory.create(dccTicketingSharedViewModel = dccTicketingSharedViewModel)
        }
    )
    private val certificatesAdapter = DccTicketingCertificateSelectionAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { viewModel.closeScreen() }
            recyclerView.apply {
                adapter = certificatesAdapter
                addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            }
            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    is CloseSelectionScreen -> popBackStack()
                    is NavigateToConsentTwoFragment -> doNavigate(
                        DccTicketingCertificateSelectionFragmentDirections
                            .actionDccTicketingCertificateSelectionFragmentToDccTicketingConsentTwoFragment(
                                event.selectedCertificateContainerId
                            )
                    )
                }
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            certificatesAdapter.update(state.certificateItems)
            binding.apply {
                when {
                    state.certificateItems.isEmpty() -> bindNoCertificatesView()
                    else -> bindMatchedCertificatesViews(state.validationCondition)
                }
            }
        }
    }

    private fun FragmentDccTicketingCertificateSelectionBinding.bindMatchedCertificatesViews(
        validationCondition: DccTicketingValidationCondition?
    ) {
        val dob = validationCondition?.dob
        val familyName = validationCondition?.fnt
        val giveName = validationCondition?.gnt

        val allowedCertificatesText = validationCondition?.type?.let { types ->
            requestedCertificateTypes(
                certificateTypes = types,
                context = requireContext(),
                separator = ","
            )
        }

        selectionTitle.text = getString(R.string.dcc_ticketing_certificate_selection_provider_requirements_title)
        noCertificatesFound.visibility = View.GONE
        allowedCertificates.text = allowedCertificatesText.toString()
        birthDate.text = getString(R.string.dcc_ticketing_certificate_birthday).format(dob)
        standardizedName.text = getFullName(familyName, giveName)
        requiredCertificatesTitle.text = getString(R.string.dcc_ticketing_certificates_meeting_requirements_title)
    }

    private fun FragmentDccTicketingCertificateSelectionBinding.bindNoCertificatesView() {
        selectionTitle.text = getString(R.string.dcc_ticketing_certificate_selection_no_certificates_found_title)
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
    }
}
