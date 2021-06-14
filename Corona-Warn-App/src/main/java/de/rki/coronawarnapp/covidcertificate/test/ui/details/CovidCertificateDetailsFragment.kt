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
import de.rki.coronawarnapp.databinding.FragmentCovidCertificateDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class CovidCertificateDetailsFragment : Fragment(R.layout.fragment_covid_certificate_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding by viewBinding<FragmentCovidCertificateDetailsBinding>()
    private val args by navArgs<CovidCertificateDetailsFragmentArgs>()
    private val viewModel: CovidCertificateDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as CovidCertificateDetailsViewModel.Factory
            factory.create(
                testCertificateIdentifier = args.testCertificateIdentifier
            )
        }
    )

    private val dayOfBirthFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
            title.alpha = titleAlpha
            subtitle.alpha = subtitleAlpha
        }

        bindTravelNoticeViews()
        bindToolbar()
        setToolbarOverlay()

        viewModel.qrCode.observe(viewLifecycleOwner) { onQrCodeReady(it) }
        viewModel.errors.observe(viewLifecycleOwner) { onError(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.covidCertificate.observe(viewLifecycleOwner) { it?.let { onCertificateReady(it) } }
    }

    private fun FragmentCovidCertificateDetailsBinding.onCertificateReady(
        testCertificate: TestCertificate
    ) {
        name.text = testCertificate.run { "$lastName, $firstName" }
        birthDate.text = testCertificate.dateOfBirth.toString(dayOfBirthFormatter)
        diseaseType.text = testCertificate.targetName
        testType.text = testCertificate.testType
        testName.text = testCertificate.testName
        testManufacturer.text = testCertificate.testNameAndManufactor
        testDate.text = "%s %s".format(
            testCertificate.sampleCollectedAt.toShortDayFormat(),
            testCertificate.sampleCollectedAt.toShortTimeFormat()
        )
        testResult.text = testCertificate.testResult
        testCenter.text = testCertificate.testCenter
        certificateCountry.text = testCertificate.certificateCountry
        certificateIssuer.text = testCertificate.certificateIssuer
        certificateId.text = testCertificate.certificateId
    }

    private fun FragmentCovidCertificateDetailsBinding.onQrCodeReady(bitmap: Bitmap?) {
        qrCodeCard.apply {
            image.setImageBitmap(bitmap)
            progressBar.hide()
            bitmap?.let { image.setOnClickListener { viewModel.openFullScreen() } }
        }
    }

    private fun FragmentCovidCertificateDetailsBinding.onError(error: Throwable) {
        qrCodeCard.progressBar.hide()
        error.toErrorDialogBuilder(requireContext()).show()
    }

    private fun FragmentCovidCertificateDetailsBinding.onNavEvent(event: CovidCertificateDetailsNavigation) {
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

    private fun FragmentCovidCertificateDetailsBinding.bindTravelNoticeViews() {
        if (travelNoticeGerman.text ==
            getString(R.string.green_certificate_attribute_certificate_travel_notice_german)
        ) {
            travelNoticeGerman.setUrl(
                R.string.green_certificate_attribute_certificate_travel_notice_german,
                R.string.green_certificate_travel_notice_link_de,
                R.string.green_certificate_travel_notice_link_de
            )
        }

        if (travelNoticeEnglish.text ==
            getString(R.string.green_certificate_attribute_certificate_travel_notice_english)
        ) {
            travelNoticeEnglish.setUrl(
                R.string.green_certificate_attribute_certificate_travel_notice_english,
                R.string.green_certificate_travel_notice_link_en,
                R.string.green_certificate_travel_notice_link_en
            )
        }
    }

    private fun FragmentCovidCertificateDetailsBinding.bindToolbar() = toolbar.apply {
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
