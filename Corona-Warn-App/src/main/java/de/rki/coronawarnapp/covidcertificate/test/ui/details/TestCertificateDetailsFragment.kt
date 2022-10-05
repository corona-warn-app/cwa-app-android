package de.rki.coronawarnapp.covidcertificate.test.ui.details

import android.graphics.Color
import android.net.Uri
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
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.certificate.getValidQrCode
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.ui.common.DccValidationNoInternetErrorDialog
import de.rki.coronawarnapp.databinding.FragmentTestCertificateDetailsBinding
import de.rki.coronawarnapp.reyclebin.ui.dialog.RecycleBinDialogType
import de.rki.coronawarnapp.reyclebin.ui.dialog.show
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.bindValidityViews
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.expendedImageResource
import de.rki.coronawarnapp.util.getEuropaStarsTint
import de.rki.coronawarnapp.util.mutateDrawable
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.ui.addMenuId
import de.rki.coronawarnapp.util.ui.addNavigationIconButtonId
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

class TestCertificateDetailsFragment : Fragment(R.layout.fragment_test_certificate_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding by viewBinding<FragmentTestCertificateDetailsBinding>()
    private val args by navArgs<TestCertificateDetailsFragmentArgs>()
    private val viewModel: TestCertificateDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as TestCertificateDetailsViewModel.Factory
            factory.create(
                containerId = TestCertificateContainerId(args.certIdentifier),
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
        viewModel.covidCertificate.observe(viewLifecycleOwner) {
            when (it != null) {
                true -> onCertificateReady(it)
                false -> {
                    Timber.tag(TAG).d("Certificate is null. Closing %s", TAG)
                    popBackStack()
                }
            }
        }
    }

    private fun FragmentTestCertificateDetailsBinding.onCertificateReady(
        certificate: TestCertificate
    ) {
        qrCodeCard.bindValidityViews(
            certificate,
            onCovPassInfoAction = { onNavEvent(TestCertificateDetailsNavigation.OpenCovPassInfo) }
        )

        startValidationCheck.apply {
            isActive = certificate.isNotScreened
        }
        toolbar.menu.findItem(R.id.menu_covid_certificate_export).isEnabled = certificate.isNotScreened
        name.text = certificate.fullNameFormatted
        icaoname.text = certificate.fullNameStandardizedFormatted
        dateOfBirth.text = certificate.dateOfBirthFormatted
        diseaseType.text = certificate.targetDisease
        testType.text = certificate.testType
        testManufacturer.text = certificate.testNameAndManufacturer
        testDate.text = certificate.sampleCollectedAtFormatted
        testResult.text = certificate.testResult
        certificateCountry.text = certificate.certificateCountry
        certificateIssuer.text = certificate.certificateIssuer
        certificateId.text = certificate.uniqueCertificateIdentifier
        val localDateTime = certificate.headerExpiresAt.toLocalDateTimeUserTz()
        expirationNotice.expirationDate.text = getString(
            R.string.expiration_date,
            localDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
            localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        )

        expandedImage.setImageResource(certificate.expendedImageResource(args.colorShade))
        europaImage.setImageDrawable(
            resources.mutateDrawable(
                R.drawable.ic_eu_stars_blue,
                requireContext().getColorCompat(certificate.getEuropaStarsTint(args.colorShade))
            )
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
            image.loadAny(certificate.getValidQrCode(showBlocked = true)) {
                crossfade(true)
                loadingView(image, progressBar)
            }
            image.setOnClickListener { viewModel.openFullScreen() }
        }
    }

    override fun onPause() {
        viewModel.markAsSeen()
        super.onPause()
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
            TestCertificateDetailsNavigation.ReturnToPersonDetailsAfterRecycling -> {
                if (args.numberOfCertificates == 1) {
                    doNavigate(
                        TestCertificateDetailsFragmentDirections
                            .actionTestCertificateDetailsFragmentToPersonOverviewFragment()
                    )
                } else popBackStack()
            }
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
                        .actionTestCertificateDetailsFragmentToCertificatePdfExportInfoFragment(event.containerId)
                )
            }
            TestCertificateDetailsNavigation.OpenCovPassInfo ->
                doNavigate(
                    TestCertificateDetailsFragmentDirections
                        .actionTestCertificateDetailsFragmentToCovPassInfoFragment()
                )
        }
    }

    private fun FragmentTestCertificateDetailsBinding.bindToolbar() = toolbar.apply {
        addMenuId(R.id.certificate_detail_fragment_menu_id)
        addNavigationIconButtonId(R.id.test_certificate_detail_fragment_navigation_icon_buttonId)
        navigationIcon = resources.mutateDrawable(R.drawable.ic_back, Color.WHITE)
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
        RecycleBinDialogType.RecycleCertificateConfirmation.show(
            fragment = this,
            positiveButtonAction = { viewModel.recycleTestCertificateConfirmed() }
        )
    }

    companion object {
        private val TAG = tag<TestCertificateDetailsFragment>()

        fun uri(certIdentifier: String): Uri {
            val encodedId = URLEncoder.encode(certIdentifier, "UTF-8")
            return "cwa://test-certificate/?fromScanner=true&certIdentifier=$encodedId".toUri()
        }
    }
}
