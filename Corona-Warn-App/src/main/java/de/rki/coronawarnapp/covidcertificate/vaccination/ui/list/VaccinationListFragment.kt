package de.rki.coronawarnapp.covidcertificate.vaccination.ui.list

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.VaccinationListViewModel.Event.DeleteVaccinationEvent
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.VaccinationListViewModel.Event.NavigateBack
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.VaccinationListViewModel.Event.NavigateToQrCodeScanScreen
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.VaccinationListViewModel.Event.NavigateToVaccinationCertificateDetails
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.databinding.FragmentVaccinationListBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class VaccinationListFragment : Fragment(R.layout.fragment_vaccination_list), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<VaccinationListFragmentArgs>()
    private val binding: FragmentVaccinationListBinding by viewBinding()
    private val viewModel: VaccinationListViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as VaccinationListViewModel.Factory
            factory.create(
                personIdentifierCode = args.personIdentifierCodeSha256
            )
        }
    )

    private val vaccinationListAdapter = VaccinationListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }

            recyclerViewVaccinationList.apply {
                adapter = vaccinationListAdapter
                setupSwipe(requireContext())
            }

            viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
                bindViews(uiState)
            }

            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    is NavigateToVaccinationCertificateDetails -> doNavigate(
                        VaccinationListFragmentDirections
                            .actionVaccinationListFragmentToVaccinationDetailsFragment(event.containerId)
                    )
                    is NavigateToQrCodeScanScreen -> doNavigate(
                        VaccinationListFragmentDirections.actionVaccinationListFragmentToDccQrCodeScanFragment()
                    )
                    is VaccinationListViewModel.Event.NavigateToQrCodeFullScreen -> {
                        val navigatorExtras =
                            binding.recyclerViewVaccinationList.layoutManager?.findViewByPosition(event.positionInList)
                                ?.run {
                                    val image = findViewById<ShapeableImageView>(R.id.image)
                                    FragmentNavigatorExtras(image to image.transitionName)
                                }
                        findNavController().navigate(
                            R.id.action_global_qrCodeFullScreenFragment,
                            QrCodeFullScreenFragmentArgs(event.qrCode).toBundle(),
                            null,
                            navigatorExtras
                        )
                    }
                    is DeleteVaccinationEvent ->
                        showDeleteVaccinationDialog(event.containerId, event.position)
                    is NavigateBack -> popBackStack()
                }
            }

            viewModel.errors.observe(viewLifecycleOwner) { error ->
                error.toErrorDialogBuilder(requireContext()).show()
            }

            registerNewVaccinationButton.setOnClickListener {
                viewModel.onRegisterNewVaccinationClick()
            }
        }
    }

    private fun FragmentVaccinationListBinding.bindViews(uiState: VaccinationListViewModel.UiState) = with(uiState) {

        val hasImmunity = uiState.vaccinationStatus == VaccinatedPerson.Status.IMMUNITY

        setToolbarOverlay()

        vaccinationListAdapter.update(listItems)

        val background = if (hasImmunity) {
            R.drawable.certificate_complete_gradient
        } else {
            R.drawable.vaccination_incomplete
        }

        expandedImage.setImageResource(background)

        appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
            title.alpha = titleAlpha
            subtitle.alpha = subtitleAlpha
        }
    }

    private fun setToolbarOverlay() {

        val deviceWidth = requireContext().resources.displayMetrics.widthPixels

        val layoutParamsRecyclerView: CoordinatorLayout.LayoutParams = binding.recyclerViewVaccinationList.layoutParams
            as (CoordinatorLayout.LayoutParams)

        val textParams = binding.subtitle.layoutParams as (LinearLayout.LayoutParams)

        val divider = 2
        textParams.bottomMargin = (deviceWidth / divider) - 24 /* 24 is space between screen border and Card */
        binding.subtitle.requestLayout()

        val behavior: AppBarLayout.ScrollingViewBehavior =
            layoutParamsRecyclerView.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (deviceWidth / divider) - 24
    }

    private fun showDeleteVaccinationDialog(containerId: VaccinationCertificateContainerId, position: Int?) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.vaccination_list_deletion_dialog_title)
            setMessage(R.string.vaccination_list_deletion_dialog_message)
            setPositiveButton(R.string.vaccination_list_deletion_dialog_positive_button) { _, _ ->
                viewModel.deleteVaccination(containerId)
            }
            setNegativeButton(R.string.vaccination_list_deletion_dialog_negative_button) { _, _ -> }
            setOnDismissListener {
                position?.let { vaccinationListAdapter.notifyItemChanged(it) }
            }
        }.show()
    }

    companion object {
        fun navigationUri(personIdentifierCodeSha256: String) =
            "coronawarnapp://vaccination-list/$personIdentifierCodeSha256".toUri()
    }
}
