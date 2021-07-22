package de.rki.coronawarnapp.covidcertificate.recovery.ui.details

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.databinding.FragmentRecoveryCertificateDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.QrCodeHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class RecoveryCertificateDetailsFragment : Fragment(R.layout.fragment_recovery_certificate_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding by viewBinding<FragmentRecoveryCertificateDetailsBinding>()
    private val args by navArgs<RecoveryCertificateDetailsFragmentArgs>()
    private val viewModel: RecoveryCertificateDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as RecoveryCertificateDetailsViewModel.Factory
            factory.create(args.containerId)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        startValidationCheck.defaultButton.setOnClickListener {
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

        viewModel.qrCode.observe(viewLifecycleOwner) { onQrCodeReady(it) }
        viewModel.errors.observe(viewLifecycleOwner) { onError(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.recoveryCertificate.observe(viewLifecycleOwner) { it?.let { onCertificateReady(it) } }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onCertificateReady(
        certificate: RecoveryCertificate
    ) {

        fullname.text = certificate.fullName
        dateOfBirth.text = certificate.dateOfBirthFormatted
        recoveredFromDisease.text = certificate.targetDisease
        dateOfFirstPositiveTestResult.text = certificate.testedPositiveOnFormatted
        certificateCountry.text = certificate.certificateCountry
        certificateIssuer.text = certificate.certificateIssuer
        certificationPeriodStart.text = certificate.validFromFormatted
        certificationPeriodEnd.text = certificate.validUntilFormatted
        certificateId.text = certificate.certificateId
        if (QrCodeHelper.isInvalidOrExpired(certificate.getState())) {
            qrCodeCard.image.alpha = 0.1f
            qrCodeCard.invalidQrCodeSymbol.isVisible = true
        } else {
            qrCodeCard.invalidQrCodeSymbol.isVisible = false
        }
        expirationNotice.expirationDate.text = getString(
            R.string.expiration_date,
            certificate.headerExpiresAt.toShortDayFormat()
        )
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onQrCodeReady(bitmap: Bitmap?) {
        qrCodeCard.apply {
            image.setImageBitmap(bitmap)
            progressBar.hide()
            bitmap?.let { image.setOnClickListener { viewModel.openFullScreen() } }
        }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onError(error: Throwable) {
        startValidationCheck.isLoading = false
        qrCodeCard.progressBar.hide()
        error.toErrorDialogBuilder(requireContext()).show()
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onNavEvent(event: RecoveryCertificateDetailsNavigation) {
        when (event) {
            RecoveryCertificateDetailsNavigation.Back -> popBackStack()
            is RecoveryCertificateDetailsNavigation.FullQrCode -> {
                val certificate = viewModel.getCovidCertificate()
                if (!QrCodeHelper.isInvalidOrExpired(certificate.getState())) {
                    findNavController().navigate(
                        R.id.action_global_qrCodeFullScreenFragment,
                        QrCodeFullScreenFragmentArgs(event.qrCodeText).toBundle(),
                        null,
                        FragmentNavigatorExtras(qrCodeCard.image to qrCodeCard.image.transitionName)
                    )
                }
            }
            is RecoveryCertificateDetailsNavigation.ValidationStart -> {
                startValidationCheck.isLoading = false
                doNavigate(
                    RecoveryCertificateDetailsFragmentDirections
                        .actionRecoveryCertificateDetailsFragmentToValidationStartFragment(event.containerId)
                )
            }
        }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.bindToolbar() = toolbar.apply {
        setNavigationOnClickListener { popBackStack() }
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_recovery_certificate_delete -> {
                    showCertificateDeletionRequest()
                    true
                }
                else -> onOptionsItemSelected(it)
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

    private fun showCertificateDeletionRequest() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.recovery_certificate_details_dialog_remove_test_title)
            setMessage(R.string.recovery_certificate_details_dialog_remove_test_message)
            setNegativeButton(R.string.green_certificate_details_dialog_remove_test_button_negative) { _, _ -> }
            setPositiveButton(R.string.green_certificate_details_dialog_remove_test_button_positive) { _, _ ->
                viewModel.onDeleteRecoveryCertificateConfirmed()
            }
        }.show()
    }
}
