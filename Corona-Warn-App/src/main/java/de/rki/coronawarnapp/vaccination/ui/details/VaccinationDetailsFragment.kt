package de.rki.coronawarnapp.vaccination.ui.details

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentVaccinationDetailsBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class VaccinationDetailsFragment : Fragment(R.layout.fragment_vaccination_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<VaccinationDetailsFragmentArgs>()
    private val binding: FragmentVaccinationDetailsBinding by viewBindingLazy()
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
            deleteButton.setOnClickListener {
                showDeletionDialog()
            }

            viewModel.vaccinationCertificate.observe(viewLifecycleOwner) {
                it.certificate?.let { certificate -> bindCertificateViews(certificate) }

                appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                    title.alpha = titleAlpha
                    subtitle.alpha = subtitleAlpha
                }
                setToolbarOverlay()
                val background = if (it.isComplete) {
                    R.drawable.vaccination_compelete_gradient
                } else {
                    R.drawable.vaccination_incomplete
                }

                expandedImage.setImageResource(background)
            }
        }

    private fun showDeletionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.vaccination_details_deletion_dialog_title))
            .setMessage(getString(R.string.vaccination_details_deletion_dialog_message))
            .setPositiveButton(getString(R.string.vaccination_details_deletion_dialog_positive_button)) { _, _ ->
                Toast.makeText(requireContext(), "TODO \uD83D\uDEA7", Toast.LENGTH_LONG).show()
                viewModel.deleteVaccination()
            }
            .setNegativeButton(getString(R.string.vaccination_details_deletion_dialog_negative_button)) { _, _ ->
                // No-Op
            }
            .show()
    }

    private fun FragmentVaccinationDetailsBinding.bindCertificateViews(
        certificate: VaccinationCertificate
    ) {
        name.text = certificate.run { "$firstName $lastName" }
        birthDate.text = getString(
            R.string.vaccination_details_birth_date,
            certificate.dateOfBirth.toString(format)
        )
        vaccinatedAt.text = certificate.vaccinatedAt.toString(format)
        vaccineName.text = certificate.vaccineName
        vaccineManufacturer.text = certificate.vaccineManufacturer
        chargeId.text = "Removed?"
        certificateIssuer.text = certificate.certificateIssuer
        certificateCountry.text = certificate.certificateCountry.getLabel(requireContext())
        certificateId.text = certificate.certificateId
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

    companion object {
        private val format = DateTimeFormat.shortDate()
    }
}
