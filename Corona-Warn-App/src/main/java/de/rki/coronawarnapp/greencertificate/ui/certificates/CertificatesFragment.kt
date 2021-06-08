package de.rki.coronawarnapp.greencertificate.ui.certificates

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentCertificatesBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.tryHumanReadableError
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
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            adapter = certificatesAdapter
        }

        viewModel.screenItems.observe2(this) { items -> certificatesAdapter.update(items) }
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
                is CertificatesFragmentEvents.ShowRefreshErrorCertificateDialog -> {
                    // TODO: error text?
                    val errorText = "Grund"
                    val dialog = DialogHelper.DialogInstance(
                        context = requireContext(),
                        title = R.string.test_certificate_refresh_dialog_title,
                        message = errorText,
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
}
