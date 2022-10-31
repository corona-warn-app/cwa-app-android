package de.rki.coronawarnapp.profile.ui.qrcode

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.loadAny
import com.google.android.material.appbar.AppBarLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ProfileQrCodeFragmentBinding
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.qrcode.ui.QrCodeScannerFragmentDirections
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.joinToSpannable
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import kotlin.math.abs

class ProfileQrCodeFragment : Fragment(R.layout.profile_qr_code_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val binding: ProfileQrCodeFragmentBinding by viewBinding()
    private val navArgs by navArgs<ProfileQrCodeFragmentArgs>()
    private val viewModel: ProfileQrCodeFragmentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ProfileQrCodeFragmentViewModel.Factory
            factory.create(navArgs.profileId)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setToolbarOverlay()
        binding.apply {
            appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                val alpha = 1.0f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.5f))
                title.alpha = alpha
            }

            nextButton.setOnClickListener { viewModel.onNext() }
            toolbar.setNavigationOnClickListener { viewModel.onClose() }
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.profile_edit -> findNavController().navigate(
                        ProfileQrCodeFragmentDirections
                            .actionProfileQrCodeFragmentToProfileCreateFragment(navArgs.profileId)
                    )
                    R.id.profile_delete -> confirmDeletionDialog()
                    R.id.profile_information -> findNavController().navigate(
                        ProfileQrCodeFragmentDirections.actionProfileQrCodeFragmentToProfileOnboardingFragment(
                            showButton = false
                        )
                    )
                }
                true
            }

            qrCodeImage.setOnClickListener {
                viewModel.openFullScreen()
            }
        }
        viewModel.personProfile.observe(viewLifecycleOwner) { personProfile ->
            with(binding) {
                personProfile.profile.let { bindPersonInfo(it) }

                val request = personProfile?.qrCode?.let { CoilQrCode(content = it) }
                qrCodeImage.loadAny(request) {
                    crossfade(true)
                    loadingView(qrCodeImage, progressBar)
                }
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                ProfileQrCodeNavigation.Back -> popBackStack()
                is ProfileQrCodeNavigation.OpenScanner -> {
                    findNavController().navigate(
                        R.id.action_to_universal_scanner,
                        QrCodeScannerFragmentDirections.actionToUniversalScanner(it.personName).arguments,
                        null,
                        FragmentNavigatorExtras(binding.nextButton to binding.nextButton.transitionName)
                    )
                }
                is ProfileQrCodeNavigation.FullQrCode -> findNavController().navigate(
                    R.id.action_global_qrCodeFullScreenFragment,
                    QrCodeFullScreenFragmentArgs(it.qrCode).toBundle(),
                    null,
                    FragmentNavigatorExtras(binding.qrCodeImage to binding.qrCodeImage.transitionName)
                )
            }
        }
    }

    private fun confirmDeletionDialog() = displayDialog {
        title(R.string.rat_qr_code_profile_dialog_title)
        message(R.string.rat_qr_code_profile_dialog_message)
        positiveButton(R.string.rat_qr_code_profile_dialog_positive_button) { viewModel.deleteProfile() }
        negativeButton(R.string.rat_qr_code_profile_dialog_negative_button)
        setDeleteDialog(true)
    }

    private fun bindPersonInfo(profile: Profile) = with(profile) {
        val name = buildSpannedString { bold { append("$firstName $lastName".trim()) } }
        val birthDate = birthDate?.let {
            getString(
                R.string.rat_qr_code_profile_birth_date,
                birthDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            )
        }.orEmpty()

        val address = "$zipCode $city".trim()
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
