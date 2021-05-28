package de.rki.coronawarnapp.greencertificate.ui.certificates

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentCertificatesBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.findNestedGraph
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import de.rki.coronawarnapp.vaccination.ui.list.VaccinationListFragment
import javax.inject.Inject

class CertificatesFragment : Fragment(R.layout.fragment_certificates), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<CertificatesViewModel> { viewModelFactory }
    private val binding by viewBinding<FragmentCertificatesBinding>()

    private val certificatesAdapter = CertificatesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            adapter = certificatesAdapter
        }

        viewModel.screenItems.observe2(this) { certificatesAdapter.update(it) }
        viewModel.events.observe2(this) { event ->
            when (event) {
                is CertificatesFragmentEvents.GoToVaccinationList -> findNavController().navigate(
                    VaccinationListFragment.navigationUri(event.personIdentifierCodeSha256)
                )
                is CertificatesFragmentEvents.OpenVaccinationRegistrationGraph -> {
                    // TODO: update when certifications info screen is done
                    findNestedGraph(R.id.vaccination_nav_graph).startDestination = R.id.vaccinationQrCodeScanFragment
                    doNavigate(CertificatesFragmentDirections.actionCertificatesFragmentToVaccinationNavGraph())
                }
            }
        }
    }
}
