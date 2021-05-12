package de.rki.coronawarnapp.vaccination.ui.details

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
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
            toolbar.setNavigationOnClickListener { viewModel.onClose() }

            viewModel.vaccinationCertificate.observe(viewLifecycleOwner) {
                it.certificate?.let { certificate -> bindCertificateViews(certificate) }
                val background = when {
                    it.isComplete -> R.drawable.vaccination_compelete_gradient
                    else -> R.drawable.vaccination_incomplete
                }
                expandedImage.setImageResource(background)
            }

            appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                title.alpha = titleAlpha
                subtitle.alpha = subtitleAlpha
            }
            setToolbarOverlay()

            viewModel.errors.observe(viewLifecycleOwner) {
                it.toErrorDialogBuilder(requireContext()).show()
            }

            viewModel.events.observe(viewLifecycleOwner) {
                when (it) {
                    VaccinationDetailsNavigation.Back -> popBackStack()
                }
            }
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
        certificateIssuer.text = certificate.certificateIssuer
        certificateCountry.text = certificate.certificateCountry.getLabel(requireContext())
        certificateId.text = certificate.certificateId
        title.text = getString(
            R.string.vaccination_details_title,
            certificate.doseNumber,
            certificate.totalSeriesOfDoses
        )
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
        private val format = DateTimeFormat.forPattern("dd.MM.yyyy")
    }
}
