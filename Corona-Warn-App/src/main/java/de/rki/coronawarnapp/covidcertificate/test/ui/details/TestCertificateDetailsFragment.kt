package de.rki.coronawarnapp.covidcertificate.test.ui.details

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.FragmentTestCertificateDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toHyphenSeparatedDate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class TestCertificateDetailsFragment : Fragment(R.layout.fragment_test_certificate_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding by viewBinding<FragmentTestCertificateDetailsBinding>()
    private val args by navArgs<TestCertificateDetailsFragmentArgs>()
    private val viewModel: CovidCertificateDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as CovidCertificateDetailsViewModel.Factory
            factory.create(
                testCertificateIdentifier = args.testCertificateIdentifier
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
            title.alpha = titleAlpha
            subtitle.alpha = subtitleAlpha
        }

        bindToolbar()
        setToolbarOverlay()

        viewModel.qrCode.observe(viewLifecycleOwner) { onQrCodeReady(it) }
        viewModel.errors.observe(viewLifecycleOwner) { onError(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.covidCertificate.observe(viewLifecycleOwner) { it?.let { onCertificateReady(it) } }
    }

    private fun FragmentTestCertificateDetailsBinding.onCertificateReady(
        testCertificate: TestCertificate
    ) {
        val testDateTime = testCertificate.sampleCollectedAt.toUserTimeZone()

        name.text = testCertificate.run { "$lastName, $firstName" }
        birthDate.text = testCertificate.dateOfBirth.toHyphenSeparatedDate()
        diseaseType.text = testCertificate.targetName
        testType.text = testCertificate.testType
        testName.text = testCertificate.testName
        testManufacturer.text = testCertificate.testNameAndManufacturer
        testDate.text = "%s %s".format(
            testDateTime.toDayFormat(),
            testDateTime.toShortTimeFormat()
        )
        testResult.text = testCertificate.testResult
        testCenter.text = testCertificate.testCenter
        certificateCountry.text = testCertificate.certificateCountry
        certificateIssuer.text = testCertificate.certificateIssuer
        certificateId.text = testCertificate.certificateId
    }

    private fun FragmentTestCertificateDetailsBinding.onQrCodeReady(bitmap: Bitmap?) {
        qrCodeCard.apply {
            image.setImageBitmap(bitmap)
            progressBar.hide()
            bitmap?.let { image.setOnClickListener { viewModel.openFullScreen() } }
        }
    }

    private fun FragmentTestCertificateDetailsBinding.onError(error: Throwable) {
        qrCodeCard.progressBar.hide()
        error.toErrorDialogBuilder(requireContext()).show()
    }

    private fun FragmentTestCertificateDetailsBinding.onNavEvent(event: CovidCertificateDetailsNavigation) {
        when (event) {
            CovidCertificateDetailsNavigation.Back -> popBackStack()
            is CovidCertificateDetailsNavigation.FullQrCode -> findNavController().navigate(
                R.id.action_global_qrCodeFullScreenFragment,
                QrCodeFullScreenFragmentArgs(event.qrCodeText).toBundle(),
                null,
                FragmentNavigatorExtras(qrCodeCard.image to qrCodeCard.image.transitionName)
            )
        }
    }

    private fun FragmentTestCertificateDetailsBinding.bindToolbar() = toolbar.apply {
        setNavigationOnClickListener { popBackStack() }
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_covid_certificate_delete -> {
                    DialogHelper.showDialog(deleteTestConfirmationDialog)
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

    private val deleteTestConfirmationDialog by lazy {
        DialogHelper.DialogInstance(
            requireActivity(),
            R.string.green_certificate_details_dialog_remove_test_title,
            R.string.green_certificate_details_dialog_remove_test_message,
            R.string.green_certificate_details_dialog_remove_test_button_positive,
            R.string.green_certificate_details_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                viewModel.onDeleteTestConfirmed()
            }
        )
    }
}
