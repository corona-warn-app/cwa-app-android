package de.rki.coronawarnapp.greencertificate.ui.certificates.details

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentGreencertificateDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import de.rki.coronawarnapp.vaccination.ui.details.VaccinationDetailsNavigation
import javax.inject.Inject

class GreenCertificateDetailsFragment : Fragment(R.layout.fragment_greencertificate_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: FragmentGreencertificateDetailsBinding by viewBinding()
    private val viewModel: GreenCertificateDetailsViewModel by cwaViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            qrCodeCard.title.text = getString(R.string.detail_green_certificate_card_title)
            qrCodeCard.subtitle.text = "Test durchgeführt am 12.05.21 18:01" // will be changed

            binding.toolbar.overflowIcon?.setTint(Color.WHITE)

            appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                title.alpha = titleAlpha
                subtitle.alpha = subtitleAlpha
            }

            if (travelNoticeGerman.text ==
                requireContext().getString(R.string.green_certificate_attribute_certificate_travel_notice_german)
            ) {
                travelNoticeGerman.setUrl(
                    R.string.green_certificate_attribute_certificate_travel_notice_german,
                    R.string.green_certificate_travel_notice_link_de,
                    R.string.green_certificate_travel_notice_link_de
                )
            }

            if (travelNoticeEnglish.text ==
                requireContext().getString(R.string.green_certificate_attribute_certificate_travel_notice_english)
            ) {
                travelNoticeEnglish.setUrl(
                    R.string.green_certificate_attribute_certificate_travel_notice_english,
                    R.string.green_certificate_travel_notice_link_en,
                    R.string.green_certificate_travel_notice_link_en
                )
            }

            binding.apply {
                setupMenu(toolbar)
            }

            // TODO: Will in the future be called when the data is loaded from the database
            viewModel.generateQrCode()

            setToolbarOverlay()

            viewModel.qrCode.observe(viewLifecycleOwner) {
                qrCodeCard.image.setImageBitmap(it)
                qrCodeCard.image.setOnClickListener { viewModel.openFullScreen() }
                qrCodeCard.progressBar.hide()
            }

            viewModel.events.observe(viewLifecycleOwner) {
                when (it)  {
                    GreenCertificateDetailsNavigation.Back -> popBackStack()
                    is GreenCertificateDetailsNavigation.FullQrCode -> findNavController().navigate(
                        R.id.action_global_qrCodeFullScreenFragment,
                        QrCodeFullScreenFragmentArgs(it.qrCodeText).toBundle(),
                        null,
                        FragmentNavigatorExtras(qrCodeCard.image to qrCodeCard.image.transitionName)
                    )
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

    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        inflateMenu(R.menu.menu_green_certificate_detail)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_green_certificate_delete -> {
                    DialogHelper.showDialog(deleteTestConfirmationDialog)
                    true
                }
                else -> onOptionsItemSelected(it)
            }
        }
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
