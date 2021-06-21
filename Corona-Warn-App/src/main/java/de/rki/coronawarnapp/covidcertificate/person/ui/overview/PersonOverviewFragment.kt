package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateServerException
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CameraPermissionCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import de.rki.coronawarnapp.databinding.PersonOverviewFragmentBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openAppDetailsSettings
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

// Shows a list of multiple persons
class PersonOverviewFragment : Fragment(R.layout.person_overview_fragment), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: PersonOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding by viewBinding<PersonOverviewFragmentBinding>()
    private val personOverviewAdapter = PersonOverviewAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            bindToolbar()
            bindRecycler()
            scanQrcodeFab.setOnClickListener { viewModel.onScanQrCode() }
        }
        viewModel.personCertificates.observe(viewLifecycleOwner) { binding.bindViews(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.markNewCertsAsSeen.observe(viewLifecycleOwner) {
            /**
             * This just needs to stay subscribed while the UI is open.
             * It causes new certificates to be marked seen automatically.
             */
        }
    }

    private fun onNavEvent(event: PersonOverviewFragmentEvents) {
        when (event) {
            is OpenPersonDetailsFragment -> doNavigate(
                PersonOverviewFragmentDirections.actionPersonOverviewFragmentToVaccinationListFragment(
                    event.personIdentifier
                )
            )
            is ShowDeleteDialog -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.test_certificate_delete_dialog_title)
                .setMessage(R.string.test_certificate_delete_dialog_body)
                .setNegativeButton(R.string.test_certificate_delete_dialog_cancel_button) { _, _ -> }
                .setCancelable(false)
                .setPositiveButton(R.string.test_certificate_delete_dialog_confirm_button) { _, _ ->
                    viewModel.deleteTestCertificate(event.containerId)
                }
                .show()

            is ShowRefreshErrorDialog -> {
                event.error.toErrorDialogBuilder(requireContext()).apply {
                    setTitle(R.string.test_certificate_refresh_dialog_title)
                    setCancelable(false)
                    if (
                        event.error is TestCertificateServerException &&
                        event.error.errorCode == TestCertificateServerException.ErrorCode.DCC_NOT_SUPPORTED_BY_LAB
                    ) {
                        setNeutralButton(R.string.test_certificate_error_invalid_labid_faq) { _, _ ->
                            openUrl(getString(R.string.test_certificate_error_invalid_labid_faq_link))
                        }
                    }
                }.show()
            }

            ScanQrCode -> doNavigate(
                PersonOverviewFragmentDirections.actionPersonOverviewFragmentToDccQrCodeScanFragment()
            )
            OpenAppDeviceSettings -> openAppDetailsSettings()
        }
    }

    private fun PersonOverviewFragmentBinding.bindToolbar() {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_information -> doNavigate(
                    PersonOverviewFragmentDirections.actionPersonOverviewFragmentToVaccinationConsentFragment(false)
                ).run { true }

                else -> onOptionsItemSelected(it)
            }
        }
    }

    private fun PersonOverviewFragmentBinding.bindViews(items: List<CertificatesItem>) {
        scanQrcodeFab.isGone = items.any { it is CameraPermissionCard.Item }
        emptyLayout.isVisible = items.isEmpty()
        personOverviewAdapter.update(items)
    }

    private fun PersonOverviewFragmentBinding.bindRecycler() = recyclerView.apply {
        adapter = personOverviewAdapter
        addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
        itemAnimator = DefaultItemAnimator()

        with(scanQrcodeFab) { onScroll { extend -> if (extend) extend() else shrink() } }
    }
}
