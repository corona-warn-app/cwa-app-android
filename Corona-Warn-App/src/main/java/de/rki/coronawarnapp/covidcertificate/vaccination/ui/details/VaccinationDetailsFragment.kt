package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.loadAny
import com.google.android.material.appbar.AppBarLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.certificate.getValidQrCode
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.ui.common.dccValidationNoInternetDialog
import de.rki.coronawarnapp.databinding.FragmentVaccinationDetailsBinding
import de.rki.coronawarnapp.reyclebin.ui.dialog.recycleCertificateDialog
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.bindValidityViews
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.mutateDrawable
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.ui.addMenuId
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.net.URLEncoder
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

class VaccinationDetailsFragment : Fragment(R.layout.fragment_vaccination_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<VaccinationDetailsFragmentArgs>()
    private val binding: FragmentVaccinationDetailsBinding by viewBinding()
    private val viewModel: VaccinationDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as VaccinationDetailsViewModel.Factory
            factory.create(
                containerId = VaccinationCertificateContainerId(args.certIdentifier),
                fromScanner = args.fromScanner
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            bindToolbar()
            setToolbarOverlay()

            viewModel.vaccinationCertificate.observe(viewLifecycleOwner) {
                if (it == null) {
                    Timber.tag(TAG).d("Certificate is null. Closing %s", TAG)
                    popBackStack()
                    return@observe
                }

                bindCertificateViews(it)
                val stateInValid = !it.isDisplayValid
                val isColorDefined = args.colorShade != PersonColorShade.COLOR_UNDEFINED

                val (background, starsTint) = when {
                    isColorDefined -> args.colorShade.background to args.colorShade.starsTint
                    stateInValid -> R.drawable.vaccination_incomplete to R.color.starsColorInvalid
                    else -> PersonColorShade.COLOR_1.background to PersonColorShade.COLOR_1.starsTint
                }

                expandedImage.setImageResource(background)
                europaImage.setImageDrawable(
                    resources.mutateDrawable(
                        R.drawable.ic_eu_stars_blue,
                        requireContext().getColorCompat(starsTint)
                    )
                )

                qrCodeCard.apply {
                    val request = it.getValidQrCode(showBlocked = true)
                    image.loadAny(request) {
                        crossfade(true)
                        loadingView(image, progressBar)
                    }
                    image.setOnClickListener { viewModel.openFullScreen() }
                }
            }

            startValidationCheck.setOnClickListener {
                startValidationCheck.isLoading = true
                viewModel.startValidationRulesDownload()
            }

            appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                title.alpha = titleAlpha
                subtitle.alpha = subtitleAlpha
                europaImage.alpha = subtitleAlpha
            }

            viewModel.errors.observe(viewLifecycleOwner) {
                startValidationCheck.isLoading = false
                qrCodeCard.progressBar.hide()
                if (it is DccValidationException && it.errorCode == DccValidationException.ErrorCode.NO_NETWORK) {
                    dccValidationNoInternetDialog()
                } else {
                    displayDialog(dialog = it.toErrorDialogBuilder(requireContext()))
                }
            }

            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    VaccinationDetailsNavigation.Back -> popBackStack()
                    VaccinationDetailsNavigation.ReturnToPersonDetailsAfterRecycling -> {
                        if (args.numberOfCertificates == 1) {
                            doNavigate(
                                VaccinationDetailsFragmentDirections
                                    .actionVaccinationDetailsFragmentToPersonOverviewFragment()
                            )
                        } else popBackStack()
                    }
                    is VaccinationDetailsNavigation.FullQrCode -> findNavController().navigate(
                        R.id.action_global_qrCodeFullScreenFragment,
                        QrCodeFullScreenFragmentArgs(event.qrCode).toBundle(),
                        null,
                        FragmentNavigatorExtras(qrCodeCard.image to qrCodeCard.image.transitionName)
                    )
                    is VaccinationDetailsNavigation.ValidationStart -> {
                        startValidationCheck.isLoading = false
                        doNavigate(
                            VaccinationDetailsFragmentDirections
                                .actionVaccinationDetailsFragmentToValidationStartFragment(event.containerId)
                        )
                    }
                    is VaccinationDetailsNavigation.Export -> {
                        doNavigate(
                            VaccinationDetailsFragmentDirections
                                .actionVaccinationDetailsFragmentToCertificatePdfExportInfoFragment(event.containerId)
                        )
                    }
                    VaccinationDetailsNavigation.OpenCovPassInfo ->
                        doNavigate(
                            VaccinationDetailsFragmentDirections.actionVaccinationDetailsFragmentToCovPassInfoFragment()
                        )
                }
            }
        }

    override fun onPause() {
        viewModel.markAsSeen()
        super.onPause()
    }

    private fun FragmentVaccinationDetailsBinding.bindToolbar() = toolbar.apply {
        addMenuId(R.id.certificate_detail_fragment_menu_id)
        toolbar.navigationIcon = resources.mutateDrawable(R.drawable.ic_back, Color.WHITE)
        setNavigationOnClickListener { viewModel.onClose() }
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_covid_certificate_delete -> {
                    showCertificateDeletionRequest()
                    true
                }
                R.id.menu_covid_certificate_export -> {
                    viewModel.onExport()
                    true
                }
                else -> false
            }
        }
    }

    private fun FragmentVaccinationDetailsBinding.bindCertificateViews(certificate: VaccinationCertificate) {
        startValidationCheck.apply {
            isActive = certificate.isNotScreened
        }
        toolbar.menu.findItem(R.id.menu_covid_certificate_export).isEnabled = certificate.isNotScreened
        qrCodeCard.bindValidityViews(
            certificate,
            onCovPassInfoAction = { viewModel.onCovPassInfoAction() }
        )
        fullname.text = certificate.fullNameFormatted
        icaoname.text = certificate.fullNameStandardizedFormatted
        dateOfBirth.text = certificate.dateOfBirthFormatted
        vaccineName.text = certificate.vaccineTypeName
        medicalProductName.text = certificate.medicalProductName
        targetDisease.text = certificate.targetDisease
        vaccineManufacturer.text = certificate.vaccineManufacturer
        vaccinationNumber.text = getString(
            R.string.vaccination_certificate_attribute_dose_number,
            certificate.doseNumber,
            certificate.totalSeriesOfDoses
        )
        vaccinatedAt.text = certificate.vaccinatedOnFormatted
        certificateCountry.text = certificate.certificateCountry
        certificateIssuer.text = certificate.certificateIssuer
        certificateId.text = certificate.uniqueCertificateIdentifier
        val localDateTime = certificate.headerExpiresAt.toLocalDateTimeUserTz()
        expirationNotice.expirationDate.text = getString(
            R.string.expiration_date,
            localDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
            localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        )
    }

    private fun setToolbarOverlay() {
        val width = requireContext().resources.displayMetrics.widthPixels

        val params: CoordinatorLayout.LayoutParams = binding.scrollView.layoutParams
            as (CoordinatorLayout.LayoutParams)

        val textParams = binding.subtitle.layoutParams as (LinearLayout.LayoutParams)
        textParams.bottomMargin = (width / 2) - 24 /* 24 is space between screen border and QrCode */
        binding.subtitle.requestLayout() /* 24 is space between screen border and QrCode */

        val behavior: AppBarLayout.ScrollingViewBehavior = params.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (width / 2) - 24
    }

    private fun showCertificateDeletionRequest() =
        recycleCertificateDialog { viewModel.recycleVaccinationCertificateConfirmed() }

    companion object {
        private val TAG = tag<VaccinationDetailsFragment>()

        fun uri(certIdentifier: String): Uri {
            val encodedId = URLEncoder.encode(certIdentifier, "UTF-8")
            return "cwa://vaccination-certificate/?fromScanner=true&certIdentifier=$encodedId".toUri()
        }
    }
}
