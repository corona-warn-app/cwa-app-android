package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragmentArgs
import de.rki.coronawarnapp.databinding.PersonOverviewFragmentBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
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
        }
        viewModel.uiState.observe(viewLifecycleOwner) { binding.bindViews(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkExpiration()
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
            is ShowDeleteDialog -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.test_certificate_delete_dialog_title)
                .setMessage(R.string.test_certificate_delete_dialog_body)
                .setNegativeButton(R.string.test_certificate_delete_dialog_cancel_button) { _, _ -> }
                .setCancelable(false)
                .setPositiveButton(R.string.test_certificate_delete_dialog_confirm_button) { _, _ ->
                    viewModel.deleteTestCertificate(event.containerId)
                }
                .show()

            is ShowRefreshErrorDialog -> event.error.toErrorDialogBuilder(requireContext()).apply {
                setTitle(R.string.test_certificate_refresh_dialog_title)
                setCancelable(false)
                if (event.showTestCertificateFaq)
                    setNeutralButton(R.string.test_certificate_error_invalid_labid_faq) { _, _ ->
                        openUrl(getString(R.string.test_certificate_error_invalid_labid_faq_link))
                    }
            }.show()

            OpenCovPassInfo -> doNavigate(
                PersonOverviewFragmentDirections.actionPersonOverviewFragmentToCovPassInfoFragment()
            )
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

                else -> onOptionsItemSelected(it)
            }
        }
    }

    private fun PersonOverviewFragmentBinding.bindViews(uiState: PersonOverviewViewModel.UiState) {
        when (uiState) {
            is PersonOverviewViewModel.UiState.Done -> {
                emptyLayout.isVisible = uiState.personCertificates.isEmpty()
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

    companion object {
        private const val TAG = "PersonOverviewFragment"
    }
}
