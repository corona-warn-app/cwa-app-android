package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.databinding.RatProfileQrCodeFragmentBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.joinToSpannable
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject
import kotlin.math.abs

class RATProfileQrCodeFragment : Fragment(R.layout.rat_profile_qr_code_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: RATProfileQrCodeFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: RatProfileQrCodeFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setToolbarOverlay()
        binding.apply {
            appBarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    val alpha = 1.0f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.5f))
                    title.alpha = alpha
                }
            )

            nextButton.setOnClickListener { viewModel.onNext() }
            toolbar.setNavigationOnClickListener { viewModel.onClose() }
            toolbar.setOnMenuItemClickListener {
                confirmDeletionDialog()
                true
            }

            qrCodeImage.setOnClickListener {
                viewModel.openFullScreen()
            }
        }
        viewModel.profile.observe(viewLifecycleOwner) { personProfile ->
            with(binding) {
                progressBar.hide()
                personProfile.profile?.let { bindPersonInfo(it) }
                qrCodeImage.setImageBitmap(personProfile.bitmap)
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                ProfileQrCodeNavigation.Back -> popBackStack()
                ProfileQrCodeNavigation.SubmissionConsent ->
                    findNavController().navigate(R.id.submissionConsentFragment)
                is ProfileQrCodeNavigation.FullQrCode -> findNavController().navigate(
                    R.id.action_global_qrCodeFullScreenFragment,
                    QrCodeFullScreenFragmentArgs(it.qrcodeText).toBundle(),
                    null,
                    FragmentNavigatorExtras(binding.qrCodeImage to binding.qrCodeImage.transitionName)
                )
            }
        }
    }

    private fun confirmDeletionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.rat_qr_code_profile_dialog_title))
            .setMessage(getString(R.string.rat_qr_code_profile_dialog_message))
            .setPositiveButton(getString(R.string.rat_qr_code_profile_dialog_positive_button)) { _, _ ->
                viewModel.deleteProfile()
            }
            .setNegativeButton(getString(R.string.rat_qr_code_profile_dialog_negative_button)) { _, _ ->
                // No-Op
            }
            .show()
    }

    private fun bindPersonInfo(ratProfile: RATProfile) = with(ratProfile) {
        val name = buildSpannedString { bold { append("$firstName $lastName") } }
        val birthDate = birthDate?.let {
            getString(
                R.string.rat_qr_code_profile_birth_date,
                birthDate.toString("dd.MM.yyyy").orEmpty()
            )
        }.orEmpty()

        val address = "$zipCode $city"
        binding.profileInfo.text = arrayOf(name, birthDate, street, address, phone, email)
            .filter { it.isNotBlank() }
            .joinToSpannable("\n")
    }

    private fun setToolbarOverlay() {
        val width = requireContext().resources.displayMetrics.widthPixels

        val params: CoordinatorLayout.LayoutParams = binding.nestedScrollView.layoutParams
            as (CoordinatorLayout.LayoutParams)

        val textParams = binding.title.layoutParams as (LinearLayout.LayoutParams)
        textParams.bottomMargin = (width / 2) - 24 /* 24 is space between screen border and QrCode */
        binding.title.requestLayout() /* 24 is space between screen border and QrCode */

        val behavior: AppBarLayout.ScrollingViewBehavior = params.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (width / 2) - 24
    }
}
