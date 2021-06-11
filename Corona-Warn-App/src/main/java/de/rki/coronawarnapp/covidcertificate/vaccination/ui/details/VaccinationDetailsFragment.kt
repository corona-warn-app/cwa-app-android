package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

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
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.FragmentVaccinationDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.format.DateTimeFormat
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
                certificateId = args.vaccinationCertificateId,
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            bindTravelNoticeViews()
            bindToolbar()

            viewModel.vaccinationCertificate.observe(viewLifecycleOwner) {
                it.certificate?.let { certificate -> bindCertificateViews(certificate) }
                val background = when {
                    it.isImmune -> R.drawable.vaccination_compelete_gradient
                    else -> R.drawable.vaccination_incomplete
                }
                expandedImage.setImageResource(background)
            }

            appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                title.alpha = titleAlpha
                subtitle.alpha = subtitleAlpha
                europaImage.alpha = subtitleAlpha
            }
            setToolbarOverlay()

            viewModel.errors.observe(viewLifecycleOwner) {
                qrCodeCard.progressBar.hide()
                it.toErrorDialogBuilder(requireContext()).show()
            }
            viewModel.qrCode.observe(viewLifecycleOwner) {
                qrCodeCard.image.setImageBitmap(it)
                it?.let {
                    qrCodeCard.image.setOnClickListener { viewModel.openFullScreen() }
                    qrCodeCard.progressBar.hide()
                }
            }

            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    VaccinationDetailsNavigation.Back -> popBackStack()
                    is VaccinationDetailsNavigation.FullQrCode -> findNavController().navigate(
                        R.id.action_global_qrCodeFullScreenFragment,
                        QrCodeFullScreenFragmentArgs(event.qrCodeText).toBundle(),
                        null,
                        FragmentNavigatorExtras(qrCodeCard.image to qrCodeCard.image.transitionName)
                    )
                }
            }
        }

    private fun FragmentVaccinationDetailsBinding.bindToolbar() = toolbar.apply {
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

    private fun FragmentVaccinationDetailsBinding.bindCertificateViews(
        certificate: VaccinationCertificate
    ) {
        name.text = certificate.fullName
        dateOfBirth.text = getString(
            R.string.vaccination_details_birth_date,
            certificate.dateOfBirth.toDayFormat()
        )
        // vaccinatedAt.text = certificate.vaccinatedAt.toDayFormat()
        vaccineName.text = certificate.medicalProductName
        vaccineManufacturer.text = certificate.vaccineManufacturer
        // vaccineTypeName.text = certificate.vaccineTypeName
        certificateIssuer.text = certificate.certificateIssuer
        certificateCountry.text = certificate.certificateCountry
        certificateId.text = certificate.certificateId
    }

    private fun FragmentVaccinationDetailsBinding.bindTravelNoticeViews() {
        if (travelNoticeGerman.text ==
            getString(R.string.vaccination_certificate_attribute_certificate_travel_notice_german)
        ) {
            travelNoticeGerman.setUrl(
                R.string.vaccination_certificate_attribute_certificate_travel_notice_german,
                R.string.vaccination_certificate_travel_notice_link_de,
                R.string.vaccination_certificate_travel_notice_link_de
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

    private fun setToolbarOverlay() {
        val width = requireContext().resources.displayMetrics.widthPixels

        val params: CoordinatorLayout.LayoutParams = binding.scrollView.layoutParams
            as (CoordinatorLayout.LayoutParams)

        val textParams = binding.subtitle.layoutParams as (LinearLayout.LayoutParams)
        textParams.bottomMargin = (width / 3) - 24 /* 24 is space between screen border and QrCode */
        binding.subtitle.requestLayout() /* 24 is space between screen border and QrCode */

        val behavior: AppBarLayout.ScrollingViewBehavior = params.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (width / 3) - 24
    }

    private val deleteTestConfirmationDialog by lazy {
        DialogHelper.DialogInstance(
            requireActivity(),
            R.string.vaccination_list_deletion_dialog_title,
            R.string.vaccination_list_deletion_dialog_message,
            R.string.green_certificate_details_dialog_remove_test_button_positive,
            R.string.green_certificate_details_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                viewModel.deleteVaccination()
            }
        )
    }
}
