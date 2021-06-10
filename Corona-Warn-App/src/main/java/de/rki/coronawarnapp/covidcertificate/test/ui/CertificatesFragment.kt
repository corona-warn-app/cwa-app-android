package de.rki.coronawarnapp.covidcertificate.test.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.VaccinationListFragment
import de.rki.coronawarnapp.databinding.FragmentCertificatesBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.findNestedGraph
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class CertificatesFragment : Fragment(R.layout.fragment_certificates), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<CertificatesViewModel> { viewModelFactory }
    private val binding by viewBinding<FragmentCertificatesBinding>()

    private val certificatesAdapter = CertificatesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setupMenu(mainTracing)
            recyclerView.apply {
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
                adapter = certificatesAdapter
            }
        }

        viewModel.screenItems.observe2(this) { items -> certificatesAdapter.update(items) }
        viewModel.events.observe2(this) { event ->
            when (event) {
                is CertificatesFragmentEvents.GoToVaccinationList -> findNavController().navigate(
                    VaccinationListFragment.navigationUri(event.personIdentifierCodeSha256)
                )
                is CertificatesFragmentEvents.OpenVaccinationRegistrationGraph -> {
                    findNestedGraph(R.id.vaccination_nav_graph).startDestination = R.id.vaccinationQrCodeScanFragment
                    doNavigate(CertificatesFragmentDirections.actionCertificatesFragmentToVaccinationNavGraph())
                }
                is CertificatesFragmentEvents.GoToCovidCertificateDetailScreen -> {
                    doNavigate(
                        CertificatesFragmentDirections
                            .actionCertificatesFragmentToCovidCertificateDetailsFragment(event.identifier)
                    )
                }
                is CertificatesFragmentEvents.ShowRefreshErrorCertificateDialog -> {
                    val dialog = DialogHelper.DialogInstance(
                        context = requireContext(),
                        title = R.string.test_certificate_refresh_dialog_title,
                        message = event.error.localizedMessage,
                        positiveButton = R.string.test_certificate_refresh_dialog_confirm_button,
                        cancelable = false
                    )
                    DialogHelper.showDialog(dialog)
                }
                is CertificatesFragmentEvents.ShowDeleteErrorCertificateDialog -> {
                    val dialog = DialogHelper.DialogInstance(
                        context = requireContext(),
                        title = R.string.test_certificate_delete_dialog_title,
                        message = R.string.test_certificate_delete_dialog_body,
                        positiveButton = R.string.test_certificate_delete_dialog_confirm_button,
                        negativeButton = R.string.test_certificate_delete_dialog_cancel_button,
                        cancelable = false,
                        positiveButtonFunction = {
                            viewModel.deleteTestCertificate(event.identifier)
                        }
                    )
                    DialogHelper.showDialog(dialog)
                }
            }
        }
    }

    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_information -> {
                    doNavigate(CertificatesFragmentDirections.actionCertificatesFragmentToConsentFragment(false))
                    true
                }
                else -> onOptionsItemSelected(it)
            }
        }
    }
}
