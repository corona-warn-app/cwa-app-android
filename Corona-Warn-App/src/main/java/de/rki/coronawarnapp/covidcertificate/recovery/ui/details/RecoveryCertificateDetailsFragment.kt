package de.rki.coronawarnapp.covidcertificate.recovery.ui.details

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.getValidQrCode
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.ui.common.dccValidationNoInternetDialog
import de.rki.coronawarnapp.databinding.FragmentRecoveryCertificateDetailsBinding
import de.rki.coronawarnapp.reyclebin.ui.dialog.recycleCertificateDialog
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.expendedImageResource
import de.rki.coronawarnapp.util.getEuropaStarsTint
import de.rki.coronawarnapp.util.mutateDrawable
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.ui.addMenuId
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.net.URLEncoder
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class RecoveryCertificateDetailsFragment : Fragment(R.layout.fragment_recovery_certificate_details), AutoInject {

    private val binding by viewBinding<FragmentRecoveryCertificateDetailsBinding>()
    private val args by navArgs<RecoveryCertificateDetailsFragmentArgs>()
    private val viewModel: RecoveryCertificateDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as RecoveryCertificateDetailsViewModel.Factory
            factory.create(
                containerId = RecoveryCertificateContainerId(args.certIdentifier),
                fromScanner = args.fromScanner
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        startValidationCheck.setOnClickListener {
            startValidationCheck.isLoading = true
            viewModel.startValidationRulesDownload()
        }

        appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
            title.alpha = titleAlpha
            subtitle.alpha = subtitleAlpha
            europaImage.alpha = subtitleAlpha
        }

        bindToolbar()
        setToolbarOverlay()

        viewModel.errors.observe(viewLifecycleOwner) { onError(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.recoveryCertificate.observe(viewLifecycleOwner) {
            when (it != null) {
                true -> onCertificateReady(it)
                false -> {
                    Timber.tag(TAG).d("Certificate is null. Closing %s", TAG)
                    viewModel.goBack()
                }
            }
        }
    }

    override fun onPause() {
        viewModel.markAsSeen()
        super.onPause()
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onCertificateReady(
        certificate: RecoveryCertificate
    ) {
        startValidationCheck.apply {
            isActive = certificate.isNotScreened
        }
        toolbar.menu.findItem(R.id.menu_recovery_certificate_export).isEnabled = certificate.isNotScreened
        qrCodeCard.bindValidityViews(
            certificate,
            onCovPassInfoAction = { onNavEvent(RecoveryCertificateDetailsNavigation.OpenCovPassInfo) }
        )
        fullname.text = certificate.fullNameFormatted
        icaoname.text = certificate.fullNameStandardizedFormatted
        dateOfBirth.text = certificate.dateOfBirthFormatted
        recoveredFromDisease.text = certificate.targetDisease
        dateOfFirstPositiveTestResult.text = certificate.testedPositiveOnFormatted
        certificateCountry.text = certificate.certificateCountry
        certificateIssuer.text = certificate.certificateIssuer
        certificateId.text = certificate.uniqueCertificateIdentifier
        val localDateTime = certificate.headerExpiresAt.toLocalDateTimeUserTz()
        expirationNotice.setText(
            getString(
                R.string.expiration_date,
                localDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            )
        )

        expandedImage.setImageResource(certificate.expendedImageResource(args.colorShade))
        europaImage.setImageDrawable(
            resources.mutateDrawable(
                R.drawable.ic_eu_stars_blue,
                requireContext().getColorCompat(certificate.getEuropaStarsTint(args.colorShade))
            )
        )

        qrCodeCard.apply {
            val request = certificate.getValidQrCode(showBlocked = true)
            setQrImage(request) { viewModel.openFullScreen() }
        }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onError(error: Throwable) {
        startValidationCheck.isLoading = false
        qrCodeCard.hideProgressBar()
        if (error is DccValidationException && error.errorCode == DccValidationException.ErrorCode.NO_NETWORK) {
            dccValidationNoInternetDialog()
        } else {
            displayDialog { setError(error) }
        }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onNavEvent(event: RecoveryCertificateDetailsNavigation) {
        when (event) {
            RecoveryCertificateDetailsNavigation.Back -> popBackStack()
            RecoveryCertificateDetailsNavigation.ReturnToPersonDetailsAfterRecycling -> {
                if (args.numberOfCertificates == 1) {
                    findNavController().navigate(
                        RecoveryCertificateDetailsFragmentDirections
                            .actionRecoveryCertificateDetailsFragmentToPersonOverviewFragment()
                    )
                } else popBackStack()
            }

            is RecoveryCertificateDetailsNavigation.FullQrCode -> findNavController().navigate(
                R.id.action_global_qrCodeFullScreenFragment,
                QrCodeFullScreenFragmentArgs(event.qrCode).toBundle(),
                null
            )

            is RecoveryCertificateDetailsNavigation.ValidationStart -> {
                startValidationCheck.isLoading = false
                findNavController().navigate(
                    RecoveryCertificateDetailsFragmentDirections
                        .actionRecoveryCertificateDetailsFragmentToValidationStartFragment(event.containerId)
                )
            }

            is RecoveryCertificateDetailsNavigation.Export -> {
                findNavController().navigate(
                    RecoveryCertificateDetailsFragmentDirections
                        .actionRecoveryCertificateDetailsFragmentToCertificatePdfExportInfoFragment(event.containerId)
                )
            }

            RecoveryCertificateDetailsNavigation.OpenCovPassInfo ->
                findNavController().navigate(
                    RecoveryCertificateDetailsFragmentDirections
                        .actionRecoveryCertificateDetailsFragmentToCovPassInfoFragment()
                )
        }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.bindToolbar() = toolbar.apply {
        addMenuId(R.id.certificate_detail_fragment_menu_id)
        toolbar.navigationIcon = resources.mutateDrawable(R.drawable.ic_back, Color.WHITE)
        setNavigationOnClickListener { viewModel.goBack() }
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_recovery_certificate_delete -> {
                    showCertificateDeletionRequest()
                    true
                }

                R.id.menu_recovery_certificate_export -> {
                    viewModel.onExport()
                    true
                }

                else -> false
            }
        }
    }

    private fun setToolbarOverlay() {
        val width = requireContext().resources.displayMetrics.widthPixels
        val params: CoordinatorLayout.LayoutParams = binding.scrollView.layoutParams as (CoordinatorLayout.LayoutParams)

        val textParams = binding.subtitle.layoutParams as (LinearLayout.LayoutParams)
        textParams.bottomMargin = (width / 3) + 170
        binding.subtitle.requestLayout()

        val behavior: AppBarLayout.ScrollingViewBehavior = params.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (width / 3) + 170
    }

    private fun showCertificateDeletionRequest() =
        recycleCertificateDialog { viewModel.recycleRecoveryCertificateConfirmed() }

    companion object {
        private val TAG = tag<RecoveryCertificateDetailsFragment>()

        fun uri(certIdentifier: String): Uri {
            val encodedId = URLEncoder.encode(certIdentifier, "UTF-8")
            return "cwa://recovery-certificate/?fromScanner=true&certIdentifier=$encodedId".toUri()
        }
    }
}
