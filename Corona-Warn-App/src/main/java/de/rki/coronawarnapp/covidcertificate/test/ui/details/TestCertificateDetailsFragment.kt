package de.rki.coronawarnapp.covidcertificate.test.ui.details

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.loadAny
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.pdf.ui.CertificateExportErrorDialog
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.ui.common.DccValidationNoInternetErrorDialog
import de.rki.coronawarnapp.databinding.FragmentTestCertificateDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.bindValidityViews
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.europaStarsResource
import de.rki.coronawarnapp.util.expendedImageResource
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class TestCertificateDetailsFragment : Fragment(R.layout.fragment_test_certificate_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding by viewBinding<FragmentTestCertificateDetailsBinding>()
    private val args by navArgs<TestCertificateDetailsFragmentArgs>()
    private val viewModel: TestCertificateDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as TestCertificateDetailsViewModel.Factory
            factory.create(
                containerId = testCertificateContainerId()
            )
        }
    )

    private fun testCertificateContainerId(): TestCertificateContainerId =
        args.containerId ?: args.certUuid?.let { TestCertificateContainerId(it) }
            ?: throw IllegalArgumentException("Either containerId or certUuid must be provided")

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

        viewModel.errors.observe(viewLifecycleOwner) { onError(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.covidCertificate.observe(viewLifecycleOwner) { it?.let { onCertificateReady(it) } }

        viewModel.exportError.observe(viewLifecycleOwner) {
            CertificateExportErrorDialog.showDialog(
                requireContext()
            ) { openUrl(getString(R.string.certificate_export_error_dialog_faq_link)) }
        }
    }

    private fun FragmentTestCertificateDetailsBinding.onCertificateReady(
        certificate: TestCertificate
    ) {
        qrCodeCard.bindValidityViews(certificate, isCertificateDetails = true)
        name.text = certificate.fullNameFormatted
        icaoname.text = certificate.fullNameStandardizedFormatted
        dateOfBirth.text = certificate.dateOfBirthFormatted
        diseaseType.text = certificate.targetName
        testType.text = certificate.testType
        testManufacturer.text = certificate.testNameAndManufacturer
        testDate.text = certificate.sampleCollectedAtFormatted
        testResult.text = certificate.testResult
        certificateCountry.text = certificate.certificateCountry
        certificateIssuer.text = certificate.certificateIssuer
        certificateId.text = certificate.certificateId
        expandedImage.setImageResource(certificate.expendedImageResource)
        europaImage.setImageResource(certificate.europaStarsResource)
        expirationNotice.expirationDate.text = getString(
            R.string.expiration_date,
            certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortDayFormat(),
            certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortTimeFormat()
        )

        if (certificate.testName.isNullOrBlank()) {
            testName.isGone = true
            testNameTitle.isGone = true
        } else {
            testName.text = certificate.testName
            testName.isGone = false
            testNameTitle.isGone = false
        }

        if (certificate.testCenter.isNullOrBlank()) {
            testCenterTitle.isGone = true
            testCenter.isGone = true
        } else {
            testCenter.text = certificate.testCenter
            testCenter.isGone = false
            testCenterTitle.isGone = false
        }

        if (certificate.testNameAndManufacturer.isNullOrBlank()) {
            testManufacturer.isGone = true
            testManufacturerTitle.isGone = true
        } else {
            testManufacturer.text = certificate.testNameAndManufacturer
            testManufacturer.isGone = false
            testManufacturerTitle.isGone = false
        }

        qrCodeCard.apply {
            image.loadAny(certificate.qrCodeToDisplay) {
                crossfade(true)
                loadingView(image, progressBar)
            }
            image.setOnClickListener { viewModel.openFullScreen() }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.refreshCertState()
    }

    private fun FragmentTestCertificateDetailsBinding.onError(error: Throwable) {
        startValidationCheck.isLoading = false
        qrCodeCard.progressBar.hide()
        if (error is DccValidationException && error.errorCode == DccValidationException.ErrorCode.NO_NETWORK) {
            DccValidationNoInternetErrorDialog(requireContext()).show()
        } else {
            error.toErrorDialogBuilder(requireContext()).show()
        }
    }

    private fun FragmentTestCertificateDetailsBinding.onNavEvent(event: TestCertificateDetailsNavigation) {
        when (event) {
            TestCertificateDetailsNavigation.Back -> popBackStack()
            is TestCertificateDetailsNavigation.FullQrCode -> findNavController().navigate(
                R.id.action_global_qrCodeFullScreenFragment,
                QrCodeFullScreenFragmentArgs(event.qrCode).toBundle(),
                null,
                FragmentNavigatorExtras(qrCodeCard.image to qrCodeCard.image.transitionName)
            )
            is TestCertificateDetailsNavigation.ValidationStart -> {
                startValidationCheck.isLoading = false
                doNavigate(
                    TestCertificateDetailsFragmentDirections
                        .actionTestCertificateDetailsFragmentToValidationStartFragment(event.containerId)
                )
            }
            is TestCertificateDetailsNavigation.Export -> {
                doNavigate(
                    TestCertificateDetailsFragmentDirections
                        .actionTestCertificateDetailsFragmentToCertificatePdfExportInfoFragment()
                )
            }
        }
    }

    private fun FragmentTestCertificateDetailsBinding.bindToolbar() = toolbar.apply {
        setNavigationOnClickListener { popBackStack() }
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
            setTitle(R.string.green_certificate_details_dialog_remove_test_title)
            setMessage(R.string.green_certificate_details_dialog_remove_test_message)
            setNegativeButton(R.string.green_certificate_details_dialog_remove_test_button_negative) { _, _ -> }
            setPositiveButton(R.string.green_certificate_details_dialog_remove_test_button_positive) { _, _ ->
                viewModel.onDeleteTestCertificateConfirmed()
            }
        }.show()
    }

    companion object {
        fun uri(certUuid: String) = "coronawarnapp://test-certificate-details/?certUuid=$certUuid".toUri()
    }
}
