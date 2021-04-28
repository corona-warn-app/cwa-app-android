package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.databinding.RatProfileQrCodeFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject
import kotlin.math.abs

class RATProfileQrCodeFragment : Fragment(R.layout.rat_profile_qr_code_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: RATProfileQrCodeFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: RatProfileQrCodeFragmentBinding by viewBindingLazy()

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
        }
        viewModel.profile.observe(viewLifecycleOwner) { personProfile ->
            with(binding) {
                progressBar.hide()
                personProfile.profile?.let { bindViews(it) }
                qrCodeImage.setImageBitmap(personProfile.bitmap)
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                ProfileQrCodeNavigation.Back -> popBackStack()
                ProfileQrCodeNavigation.SubmissionConsent ->
                    findNavController().navigate(R.id.submissionConsentFragment)
            }
        }
    }

    private fun confirmDeletionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.rat_qr_code_profile_dialog_title))
            .setPositiveButton(getString(R.string.rat_qr_code_profile_dialog_positive_button)) { _, _ ->
                viewModel.deleteProfile()
            }
            .setNegativeButton(getString(R.string.rat_qr_code_profile_dialog_negative_button)) { _, _ ->
                // No-Op
            }
            .show()
    }

    private fun RatProfileQrCodeFragmentBinding.bindViews(ratProfile: RATProfile) {
        val nameExists = ratProfile.firstName.isNotBlank() || ratProfile.lastName.isNotBlank()
        if (nameExists) {
            name.text = getString(
                R.string.rat_qr_code_profile_name,
                ratProfile.firstName,
                ratProfile.lastName
            )
        }

        val birthDateExists = ratProfile.birthDate != null
        ratProfile.birthDate?.let {
            birthDate.text = getString(
                R.string.rat_qr_code_profile_birth_date,
                it.toString("dd.MM.yyyy").orEmpty()
            )
        }

        name.isVisible = nameExists
        birthDate.isVisible = birthDateExists
        personData.isVisible = nameExists || birthDateExists
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
