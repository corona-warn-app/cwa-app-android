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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.FragmentVaccinationDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
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
                containerId = args.containerId,
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {

            bindToolbar()
            setToolbarOverlay()

            viewModel.vaccinationCertificate.observe(viewLifecycleOwner) {
                it.certificate?.let { certificate -> bindCertificateViews(certificate) }
                val background = when {
                    it.isImmune -> R.drawable.certificate_complete_gradient
                    else -> R.drawable.vaccination_incomplete
                }

                val europaIcon = when {
                    it.isImmune -> R.drawable.ic_eu_stars_blue
                    else -> R.drawable.ic_eu_stars_grey
                }

                expandedImage.setImageResource(background)
                europaImage.setImageResource(europaIcon)
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
                    is VaccinationDetailsNavigation.ValidationStart -> doNavigate(
                        VaccinationDetailsFragmentDirections
                            .actionVaccinationDetailsFragmentToValidationStartFragment(event.containerId)
                    )
                }
            }
        }

    private fun FragmentVaccinationDetailsBinding.bindToolbar() = toolbar.apply {
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

    private fun FragmentVaccinationDetailsBinding.bindCertificateViews(
        certificate: VaccinationCertificate
    ) {
        fullname.text = certificate.fullName
        dateOfBirth.text = certificate.dateOfBirthFormatted
        medialProductName.text = certificate.medicalProductName
        vaccineTypeName.text = certificate.vaccineTypeName
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
        certificateId.text = certificate.certificateId
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
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.vaccination_list_deletion_dialog_title)
            setMessage(R.string.vaccination_list_deletion_dialog_message)
            setNegativeButton(R.string.green_certificate_details_dialog_remove_test_button_negative) { _, _ -> }
            setPositiveButton(R.string.green_certificate_details_dialog_remove_test_button_positive) { _, _ ->
                viewModel.onDeleteVaccinationCertificateConfirmed()
            }
        }.show()
    }
}
