package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.core.view.isVisible
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
import de.rki.coronawarnapp.covidcertificate.pdf.ui.CertificateExportErrorDialog
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.ui.common.DccValidationNoInternetErrorDialog
import de.rki.coronawarnapp.databinding.FragmentVaccinationDetailsBinding
import de.rki.coronawarnapp.reyclebin.ui.dialog.RecycleBinDialogType
import de.rki.coronawarnapp.reyclebin.ui.dialog.show
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.bindValidityViews
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.mutateDrawable
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.net.URLEncoder
import javax.inject.Inject

class VaccinationDetailsFragment : Fragment(R.layout.fragment_vaccination_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<VaccinationDetailsFragmentArgs>()
    private val binding: FragmentVaccinationDetailsBinding by viewBinding()
    private val viewModel: VaccinationDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as VaccinationDetailsViewModel.Factory
            Timber.d("args=$args")
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
                it.certificate?.let { certificate -> bindCertificateViews(certificate) }
                val stateInValid = it.certificate?.isDisplayValid == false
                val isFinalShot = it.certificate?.isSeriesCompletingShot == true
                val isColorDefined = args.colorShade != PersonColorShade.COLOR_UNDEFINED

                val (background, starsTint) = when {
                    isColorDefined -> args.colorShade.background to args.colorShade.starsTint
                    stateInValid -> R.drawable.vaccination_incomplete to R.color.starsColorInvalid
                    (it.isImmune && isFinalShot) -> R.drawable.certificate_complete_gradient to R.color.starsColor1
                    else -> R.drawable.vaccination_incomplete to R.color.starsColorInvalid
                }

                expandedImage.setImageResource(background)
                europaImage.setImageDrawable(
                    resources.mutateDrawable(
                        R.drawable.ic_eu_stars_blue,
                        requireContext().getColorCompat(starsTint)
                    )
                )

                qrCodeCard.apply {
                    val request = it.certificate?.getValidQrCode(showBlocked = true)
                    image.loadAny(request) {
                        crossfade(true)
                        loadingView(image, progressBar)
                    }
                    image.setOnClickListener { viewModel.openFullScreen() }
                }
            }

            startValidationCheck.defaultButton.setOnClickListener {
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
                    DccValidationNoInternetErrorDialog(requireContext()).show()
                } else {
                    it.toErrorDialogBuilder(requireContext()).show()
                }
            }

            viewModel.exportError.observe(viewLifecycleOwner) {
                CertificateExportErrorDialog.showDialog(
                    requireContext()
                ) { openUrl(getString(R.string.certificate_export_error_dialog_faq_link)) }
            }

            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    VaccinationDetailsNavigation.Back -> popBackStack()
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

    override fun onStop() {
        super.onStop()
        viewModel.refreshCertState()
    }

    private fun FragmentVaccinationDetailsBinding.bindToolbar() = toolbar.apply {
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
                else -> onOptionsItemSelected(it)
            }
        }
    }

    private fun FragmentVaccinationDetailsBinding.bindCertificateViews(
        certificate: VaccinationCertificate
    ) {
        startValidationCheck.apply {
            isEnabled = certificate.isNotBlocked
            defaultButton.isEnabled = certificate.isNotBlocked
        }
        toolbar.menu.findItem(R.id.menu_covid_certificate_export).isEnabled = certificate.isNotBlocked
        qrCodeCard.bindValidityViews(
            certificate,
            isCertificateDetails = true,
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
        oneShotInfo.isVisible = certificate.totalSeriesOfDoses == 1
        expirationNotice.expirationDate.text = getString(
            R.string.expiration_date,
            certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortDayFormat(),
            certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortTimeFormat()
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

    private fun showCertificateDeletionRequest() {
        RecycleBinDialogType.RecycleCertificateConfirmation.show(
            fragment = this,
            positiveButtonAction = { viewModel.recycleVaccinationCertificateConfirmed() }
        )
    }

    companion object {
        fun uri(certIdentifier: String): Uri {
            val encodedId = URLEncoder.encode(certIdentifier, "UTF-8")
            return "cwa://vaccination-certificate/?fromScanner=true&certIdentifier=$encodedId".toUri()
        }
    }
}
