package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.get
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.ui.common.DccValidationNoInternetErrorDialog
import de.rki.coronawarnapp.databinding.PersonDetailsFragmentBinding
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.reyclebin.ui.dialog.RecycleBinDialogType
import de.rki.coronawarnapp.reyclebin.ui.dialog.show
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.mutateDrawable
import de.rki.coronawarnapp.util.ui.doNavigate
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
            is OpenRecoveryCertificateDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToRecoveryCertificateDetailsFragment(
                        certIdentifier = event.containerId.qrCodeHash,
                        numberOfCertificates = numberOfCertificates,
                        fromScanner = false,
                        colorShade = event.colorShade
                    ).also { viewModel.dismissAdmissionStateBadge() }
            )
            is OpenTestCertificateDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToTestCertificateDetailsFragment(
                        certIdentifier = event.containerId.qrCodeHash,
                        numberOfCertificates = numberOfCertificates,
                        fromScanner = false,
                        colorShade = event.colorShade
                    ).also { viewModel.dismissAdmissionStateBadge() }
            )
            is OpenVaccinationCertificateDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToVaccinationDetailsFragment(
                        certIdentifier = event.containerId.qrCodeHash,
                        numberOfCertificates = numberOfCertificates,
                        fromScanner = false,
                        colorShade = event.colorShade
                    ).also { viewModel.dismissAdmissionStateBadge() }
            )
            is ValidationStart -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToValidationStartFragment(event.containerId)
            ).also { viewModel.dismissAdmissionStateBadge() }
            is ShowErrorDialog -> with(event) {
                if (error is DccValidationException && error.errorCode == DccValidationException.ErrorCode.NO_NETWORK) {
                    DccValidationNoInternetErrorDialog(requireContext()).show()
                } else {
                    error.toErrorDialogBuilder(requireContext()).show()
                }
            }
            is OpenBoosterInfoDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToBoosterInfoDetailsFragment(event.personIdentifierCode)
            ).also { viewModel.dismissAdmissionStateBadge() }
            is OpenCertificateReissuanceConsent -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToDccReissuanceConsentFragment(event.personIdentifierCode)
            ).also { viewModel.dismissAdmissionStateBadge() }
            Back -> {
                removeGlobalLayoutListener()
                popBackStack()
            }
            OpenCovPassInfo ->
                doNavigate(PersonDetailsFragmentDirections.actionPersonDetailsFragmentToCovPassInfoFragment())
                    .also { viewModel.dismissAdmissionStateBadge() }
            is RecycleCertificate -> showCertificateDeletionRequest(event.cwaCovidCertificate, event.position)
        }
    }

    private fun showCertificateDeletionRequest(cwaCovidCertificate: CwaCovidCertificate, position: Int) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existingNotification = notificationManager.activeNotifications
        val shouldUpdateNotification =
            existingNotification.any { it.id == NotificationConstants.DCC_STATE_CHECK_NOTIFICATION_ID }
        RecycleBinDialogType.RecycleCertificateConfirmation.show(
            fragment = this,
            positiveButtonAction = { viewModel.recycleCertificate(cwaCovidCertificate, shouldUpdateNotification) },
            negativeButtonAction = { personDetailsAdapter.notifyItemChanged(position) }
        )
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
