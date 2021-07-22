package de.rki.coronawarnapp.covidcertificate.test.ui.details

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.loadAny
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.FragmentTestCertificateDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.QrCodeHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
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
                containerId = args.containerId
            )
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

        viewModel.errors.observe(viewLifecycleOwner) { onError(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.covidCertificate.observe(viewLifecycleOwner) { it?.let { onCertificateReady(it) } }
    }

    private fun FragmentTestCertificateDetailsBinding.onCertificateReady(
        certificate: TestCertificate
    ) {
        name.text = certificate.fullName
        dateOfBirth.text = certificate.dateOfBirthFormatted
        diseaseType.text = certificate.targetName
        testType.text = certificate.testType
        testName.text = certificate.testName
        testManufacturer.text = certificate.testNameAndManufacturer
        testDate.text = certificate.sampleCollectedAtFormatted
        testResult.text = certificate.testResult
        certificateCountry.text = certificate.certificateCountry
        certificateIssuer.text = certificate.certificateIssuer
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
            var qrCodeString = certificate.qrCode
            if (QrCodeHelper.isInvalidOrExpired(viewModel.getCovidCertificate().getState())) {
                qrCodeString = QrCodeHelper.sampleQrCodeText
            }
            image.loadAny(CoilQrCode(content = qrCodeString)) {
                crossfade(true)
                loadingView(image, progressBar)
            }
            image.setOnClickListener { viewModel.openFullScreen() }
        }
    }

    private fun FragmentTestCertificateDetailsBinding.onError(error: Throwable) {
        startValidationCheck.isLoading = false
        qrCodeCard.progressBar.hide()
        error.toErrorDialogBuilder(requireContext()).show()
    }

    private fun FragmentTestCertificateDetailsBinding.onNavEvent(event: TestCertificateDetailsNavigation) {
        when (event) {
            TestCertificateDetailsNavigation.Back -> popBackStack()
            is TestCertificateDetailsNavigation.FullQrCode -> {
                val certificate = viewModel.getCovidCertificate()
                if (QrCodeHelper.isInvalidOrExpired(certificate.getState())) {
                    findNavController().navigate(
                        R.id.action_global_qrCodeFullScreenFragment,
                        QrCodeFullScreenFragmentArgs(event.qrCodeText).toBundle(),
                        null,
                        FragmentNavigatorExtras(qrCodeCard.image to qrCodeCard.image.transitionName)
                    )
                }
            }
            is TestCertificateDetailsNavigation.ValidationStart -> {
                startValidationCheck.isLoading = false
                doNavigate(
                    TestCertificateDetailsFragmentDirections
                        .actionTestCertificateDetailsFragmentToValidationStartFragment(event.containerId)
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
}
