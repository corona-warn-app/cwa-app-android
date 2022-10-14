package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.get
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.ui.common.dccValidationNoInternetDialog
import de.rki.coronawarnapp.databinding.PersonDetailsFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.mutateDrawable
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

// Shows the list of certificates for one person
class PersonDetailsFragment : Fragment(R.layout.person_details_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<PersonDetailsFragmentArgs>()
    private val binding: PersonDetailsFragmentBinding by viewBinding()
    private val viewModel: PersonDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as PersonDetailsViewModel.Factory
            factory.create(
                personIdentifierCode = args.personCode,
                colorShade = args.colorShade
            )
        }
    )
    private val personDetailsAdapter = PersonDetailsAdapter()
    private var numberOfCertificates = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val materialContainerTransform = MaterialContainerTransform()
        sharedElementEnterTransition = materialContainerTransform
        sharedElementReturnTransition = materialContainerTransform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            root.transitionName = args.personCode
            toolbar.setNavigationOnClickListener {
                viewModel.dismissAdmissionStateBadge(true)
            }
            recyclerViewCertificatesList.apply {
                adapter = personDetailsAdapter
                layoutManager = LinearLayoutManager(requireContext())
                itemAnimator = DefaultItemAnimator()
                setupSwipe(context = requireContext())
            }
            appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                title.alpha = titleAlpha
                name.alpha = titleAlpha
                europaImage.alpha = subtitleAlpha
            }

            setToolbarOverlay()
            viewModel.uiState.observe(viewLifecycleOwner) {
                name.text = it.name
                personDetailsAdapter.update(it.certificateItems)
                numberOfCertificates = it.numberOfCertificates
            }
            viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
            viewModel.currentColorShade.observe(viewLifecycleOwner) { color ->
                expandedImage.setImageResource(color.background)
                europaImage.setImageDrawable(
                    resources.mutateDrawable(
                        R.drawable.ic_eu_stars_blue,
                        requireContext().getColorCompat(color.starsTint)
                    )
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.dismissAdmissionStateBadge(true)
        }
    }

    private fun onNavEvent(event: PersonDetailsEvents) {
        when (event) {
            is OpenRecoveryCertificateDetails -> findNavController().navigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToRecoveryCertificateDetailsFragment(
                        certIdentifier = event.containerId.qrCodeHash,
                        numberOfCertificates = numberOfCertificates,
                        fromScanner = false,
                        colorShade = event.colorShade
                    ).also { viewModel.dismissAdmissionStateBadge() }
            )

            is OpenTestCertificateDetails -> findNavController().navigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToTestCertificateDetailsFragment(
                        certIdentifier = event.containerId.qrCodeHash,
                        numberOfCertificates = numberOfCertificates,
                        fromScanner = false,
                        colorShade = event.colorShade
                    ).also { viewModel.dismissAdmissionStateBadge() }
            )

            is OpenVaccinationCertificateDetails -> findNavController().navigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToVaccinationDetailsFragment(
                        certIdentifier = event.containerId.qrCodeHash,
                        numberOfCertificates = numberOfCertificates,
                        fromScanner = false,
                        colorShade = event.colorShade
                    ).also { viewModel.dismissAdmissionStateBadge() }
            )

            is ValidationStart -> findNavController().navigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToValidationStartFragment(event.containerId)
            ).also { viewModel.dismissAdmissionStateBadge() }

            is ShowErrorDialog -> with(event) {
                if (error is DccValidationException && error.errorCode == DccValidationException.ErrorCode.NO_NETWORK) {
                    dccValidationNoInternetDialog()
                } else {
                    displayDialog(dialog = error.toErrorDialogBuilder(requireContext()))
                }
            }

            is OpenBoosterInfoDetails -> findNavController().navigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToBoosterInfoDetailsFragment(event.personIdentifierCode)
            ).also { viewModel.dismissAdmissionStateBadge() }

            is OpenCertificateReissuanceConsent -> findNavController().navigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToDccReissuanceConsentFragment(event.personIdentifierCode)
            ).also { viewModel.dismissAdmissionStateBadge() }

            Back -> {
                removeGlobalLayoutListener()
                popBackStack()
            }

            OpenCovPassInfo ->
                findNavController().navigate(
                    PersonDetailsFragmentDirections.actionPersonDetailsFragmentToCovPassInfoFragment()
                ).also { viewModel.dismissAdmissionStateBadge() }

            is RecycleCertificate -> onDeleteCertificateDialog(event.cwaCovidCertificate, event.position)
        }
    }

    private fun onDeleteCertificateDialog(certificate: CwaCovidCertificate, position: Int) =
        displayDialog(
            isDeleteDialog = true,
            onDismissAction = { personDetailsAdapter.notifyItemChanged(position) }
        ) {
            setTitle(R.string.recycle_bin_recycle_certificate_dialog_title)
            setMessage(R.string.recycle_bin_recycle_certificate_dialog_message)
            setPositiveButton(R.string.recycle_bin_recycle_certificate_dialog_positive_button) { _, _ ->
                viewModel.recycleCertificate(certificate)
            }
            setNegativeButton(R.string.family_tests_list_deletion_alert_cancel_button) { _, _ -> }
        }

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        try {
            if (binding.recyclerViewCertificatesList.childCount > 0) {
                removeGlobalLayoutListener()
                val firstElement = binding.recyclerViewCertificatesList[0]
                val emptySpaceToTop =
                    firstElement.marginTop + binding.recyclerViewCertificatesList.paddingTop
                val overlap = (firstElement.height / 2) + emptySpaceToTop

                val layoutParamsRecyclerView: CoordinatorLayout.LayoutParams =
                    binding.recyclerViewCertificatesList.layoutParams
                        as (CoordinatorLayout.LayoutParams)
                val behavior: AppBarLayout.ScrollingViewBehavior =
                    layoutParamsRecyclerView.behavior as (AppBarLayout.ScrollingViewBehavior)
                behavior.overlayTop = overlap

                binding.europaImage.layoutParams.height = binding.collapsingToolbarLayout.height + overlap
                binding.europaImage.requestLayout()
            }
        } catch (e: Exception) {
            Timber.e(e, "PersonDetailsFragment can't update toolbar height")
        }
    }

    private fun removeGlobalLayoutListener() {
        binding.recyclerViewCertificatesList.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }

    private fun setToolbarOverlay() {
        binding.recyclerViewCertificatesList.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }
}
