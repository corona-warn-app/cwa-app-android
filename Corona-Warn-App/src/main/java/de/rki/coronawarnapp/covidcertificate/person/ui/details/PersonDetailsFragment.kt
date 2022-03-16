package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.ui.common.DccValidationNoInternetErrorDialog
import de.rki.coronawarnapp.databinding.PersonDetailsFragmentBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.mutateDrawable
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
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
            toolbar.setNavigationOnClickListener { popBackStack() }
            recyclerViewCertificatesList.apply {
                adapter = personDetailsAdapter
                addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
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
    }

    private fun onNavEvent(event: PersonDetailsEvents) {
        when (event) {
            is OpenRecoveryCertificateDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToRecoveryCertificateDetailsFragment(
                        certIdentifier = event.containerId.qrCodeHash,
                        fromScanner = false,
                        colorShade = event.colorShade
                    )
            )
            is OpenTestCertificateDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToTestCertificateDetailsFragment(
                        certIdentifier = event.containerId.qrCodeHash,
                        fromScanner = false,
                        colorShade = event.colorShade
                    )
            )
            is OpenVaccinationCertificateDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToVaccinationDetailsFragment(
                        certIdentifier = event.containerId.qrCodeHash,
                        fromScanner = false,
                        colorShade = event.colorShade
                    )
            )
            is ValidationStart -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToValidationStartFragment(event.containerId)
            )
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
            )
            is OpenCertificateReissuanceConsent -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToDccReissuanceConsentFragment(event.personIdentifierCode)
            )
            Back -> popBackStack()
            OpenCovPassInfo ->
                doNavigate(PersonDetailsFragmentDirections.actionPersonDetailsFragmentToCovPassInfoFragment())
        }
    }

    private fun setToolbarOverlay() {
        val deviceWidth = requireContext().resources.displayMetrics.widthPixels

        val layoutParamsRecyclerView: CoordinatorLayout.LayoutParams = binding.recyclerViewCertificatesList.layoutParams
            as (CoordinatorLayout.LayoutParams)

        val textParams = binding.toolbarLinearLayout.layoutParams as (CollapsingToolbarLayout.LayoutParams)

        val divider = 2
        textParams.bottomMargin = (deviceWidth / divider) - 24 /* 24 is space between screen border and Card */
        binding.toolbarLinearLayout.requestLayout()

        val behavior: AppBarLayout.ScrollingViewBehavior =
            layoutParamsRecyclerView.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (deviceWidth / divider) - 24
    }
}
