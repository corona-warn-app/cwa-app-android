package de.rki.coronawarnapp.greencertificate.ui.certificates.details

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentCovidCertificateDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class CovidCertificateDetailsFragment : Fragment(R.layout.fragment_covid_certificate_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding by viewBinding<FragmentCovidCertificateDetailsBinding>()
    private val viewModel: CovidCertificateDetailsViewModel by cwaViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            qrCodeCard.title.text = getString(R.string.detail_green_certificate_card_title)
            qrCodeCard.subtitle.text = "Test durchgeführt am 12.05.21 18:01" // will be changed
            appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                title.alpha = titleAlpha
                subtitle.alpha = subtitleAlpha
            }

            bindTravelNoticeViews()
            bindToolbar()
            setToolbarOverlay()

            viewModel.generateQrCode() // TODO remove 
            viewModel.qrCode.observe(viewLifecycleOwner) {
                qrCodeCard.image.setImageBitmap(it)
                qrCodeCard.image.setOnClickListener { viewModel.openFullScreen() }
                qrCodeCard.progressBar.hide()
            }

            viewModel.events.observe(viewLifecycleOwner) {
                when (it) {
                    CovidCertificateDetailsNavigation.Back -> popBackStack()
                    is CovidCertificateDetailsNavigation.FullQrCode -> findNavController().navigate(
                        R.id.action_global_qrCodeFullScreenFragment,
                        QrCodeFullScreenFragmentArgs(it.qrCodeText).toBundle(),
                        null,
                        FragmentNavigatorExtras(qrCodeCard.image to qrCodeCard.image.transitionName)
                    )
                }
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
        val params: CoordinatorLayout.LayoutParams = binding.scrollView.layoutParams
            as (CoordinatorLayout.LayoutParams)

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
