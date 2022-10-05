package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.AdmissionScenariosSharedViewModel
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragmentArgs
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.AdmissionTileProvider
import de.rki.coronawarnapp.databinding.AdmissionScenarioTileBinding
import de.rki.coronawarnapp.databinding.PersonOverviewFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

class PersonOverviewFragment : Fragment(R.layout.person_overview_fragment), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val admissionViewModel by navGraphViewModels<AdmissionScenariosSharedViewModel>(
        R.id.covid_certificates_graph
    )
    private val viewModel: PersonOverviewViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, savedState ->
            factory as PersonOverviewViewModel.Factory
            factory.create(
                admissionScenariosSharedViewModel = admissionViewModel,
                savedState = savedState
            )
        }
    )
    private val binding by viewBinding<PersonOverviewFragmentBinding>()
    private val personOverviewAdapter = PersonOverviewAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            bindToolbar()
            bindRecycler()
        }
        viewModel.uiState.observe(viewLifecycleOwner) { binding.bindViews(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.admissionTile.observe(viewLifecycleOwner) { binding.admissionContainer.bindAdmissionTile(it) }
        viewModel.isExportAllTooltipVisible.observe(viewLifecycleOwner) { visible ->
            binding.exportTooltip.root.isVisible = visible
        }
        binding.exportTooltip.close.setOnClickListener { viewModel.dismissExportAllToolTip() }
    }

    private fun onNavEvent(event: PersonOverviewFragmentEvents) {
        Timber.tag(TAG).d(" onNavEvent(event=%s)", event)
        when (event) {
            is OpenPersonDetailsFragment -> {
                setupHoldTransition()
                val navigatorExtras = binding.recyclerView.layoutManager
                    ?.findViewByPosition(event.position)?.run {
                        FragmentNavigatorExtras(this to transitionName)
                    }
                findNavController().navigate(
                    R.id.action_personOverviewFragment_to_personDetailsFragment,
                    PersonDetailsFragmentArgs(event.personIdentifier, event.colorShade).toBundle(),
                    null,
                    navigatorExtras
                )
            }

            is ShowDeleteDialog -> displayDialog(cancelable = false, isDeleteDialog = true) {
                setTitle(R.string.test_certificate_delete_dialog_title)
                setMessage(R.string.test_certificate_delete_dialog_body)
                setPositiveButton(R.string.test_certificate_delete_dialog_confirm_button) { _, _ ->
                    viewModel.deleteTestCertificate(event.containerId)
                }
                setNegativeButton(R.string.test_certificate_delete_dialog_cancel_button) { _, _ -> }
            }

            is ShowRefreshErrorDialog -> event.error.toErrorDialogBuilder(requireContext()).apply {
                setTitle(R.string.test_certificate_refresh_dialog_title)
                setCancelable(false)
                if (event.showTestCertificateFaq)
                    setNeutralButton(R.string.test_certificate_error_invalid_labid_faq) { _, _ ->
                        openUrl(getString(R.string.test_certificate_error_invalid_labid_faq_link))
                    }
            }.show()

            is ShowMigrationInfoDialog -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.certificate_migration_dialog_title)
                .setMessage(R.string.certificate_migration_dialog_message)
                .setPositiveButton(R.string.errors_generic_button_positive) { _, _ -> }
                .setCancelable(true)
                .show()

            OpenCovPassInfo -> doNavigate(
                PersonOverviewFragmentDirections.actionPersonOverviewFragmentToCovPassInfoFragment()
            )

            OpenAdmissionScenarioScreen -> {
                setupHoldTransition()
                val navigatorExtras = FragmentNavigatorExtras(
                    binding.admissionContainer.root to binding.admissionContainer.root.transitionName
                )
                findNavController().navigate(
                    R.id.action_personOverviewFragment_to_admissionScenariosFragment,
                    null,
                    null,
                    navigatorExtras
                )
            }

            is ShowAdmissionScenarioError -> event.error.toErrorDialogBuilder(requireContext()).show()
        }
    }

    private fun setupHoldTransition() {
        exitTransition = Hold()
        reenterTransition = Hold()
    }

    private fun setupAxisTransition() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    private fun PersonOverviewFragmentBinding.bindToolbar() {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_information -> {
                    setupAxisTransition()
                    doNavigate(
                        PersonOverviewFragmentDirections
                            .actionPersonOverviewFragmentToCovidCertificateOnboardingFragment(false)
                    )
                    true
                }

                R.id.menu_export_all -> {
                    setupAxisTransition()
                    doNavigate(
                        PersonOverviewFragmentDirections
                            .actionPersonOverviewFragmentToExportAllCertsPdfInfoFragment()
                    )
                    true
                }

                else -> false
            }
        }
    }

    private fun PersonOverviewFragmentBinding.bindViews(uiState: PersonOverviewViewModel.UiState) {
        when (uiState) {
            is PersonOverviewViewModel.UiState.Done -> {
                emptyLayout.isVisible = uiState.personCertificates.isEmpty()
                toolbar.menu.findItem(R.id.menu_export_all).isVisible = uiState.personCertificates.isNotEmpty()
                recyclerView.isGone = uiState.personCertificates.isEmpty()
                personOverviewAdapter.update(uiState.personCertificates)
                loadingLayoutGroup.isVisible = false
            }

            PersonOverviewViewModel.UiState.Loading -> {
                recyclerView.isGone = true
                emptyLayout.isGone = true
                loadingLayoutGroup.isVisible = true
            }
        }
    }

    private fun PersonOverviewFragmentBinding.bindRecycler() = recyclerView.apply {
        adapter = personOverviewAdapter
        addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
        itemAnimator = DefaultItemAnimator()
    }

    private fun AdmissionScenarioTileBinding.bindAdmissionTile(
        tile: AdmissionTileProvider.AdmissionTile
    ) {
        admissionTile.apply {
            isVisible = tile.visible
            setOnClickListener { viewModel.openAdmissionScenarioScreen() }
        }

        admissionTileTitle.text = tile.title.ifEmpty { getString(R.string.ccl_admission_state_tile_title) }
        admissionTileSubtitle.text = tile.subtitle.ifEmpty { getString(R.string.ccl_admission_state_tile_subtitle) }
    }

    companion object {
        private const val TAG = "PersonOverviewFragment"
    }
}
