package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerQrCodeDetailFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class QrCodeDetailFragment : Fragment(R.layout.trace_location_organizer_qr_code_detail_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val vm: QrCodeDetailViewModel by cwaViewModels { viewModelFactory }
    private val binding: TraceLocationOrganizerQrCodeDetailFragmentBinding by viewBindingLazy()

    private val qrCodeText = "HTTPS://E.CORONAWARN.APP/C1/BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUD" +
        "BOJ2HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGCPUZ2RQACAYEJ3HQYMAFF" +
        "BU2SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU7TYERH23B746RQTABO3CTI="

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val width = requireContext().resources.displayMetrics.widthPixels

        vm.createQrCode(qrCodeText)

        val params: CoordinatorLayout.LayoutParams = binding.nestedScrollView.layoutParams as (CoordinatorLayout.LayoutParams)
        val behavior: AppBarLayout.ScrollingViewBehavior = params.behavior as ((AppBarLayout.ScrollingViewBehavior))
        behavior.overlayTop = width / 2

        val textParams = binding.subtitle.layoutParams as (LinearLayout.LayoutParams)
        textParams.bottomMargin = width / 2 + 24
        binding.subtitle.requestLayout()

        binding.apply {
            appBarLayout.addOnOffsetChangedListener(
                OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    title.alpha = (
                        1.0f - Math.abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.5f))
                        )
                    subtitle.alpha = (
                        1.0f - Math.abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.7f))
                        )
                }
            )

            toolbar.apply {
                navigationIcon = context.getDrawableCompat(R.drawable.ic_close_white)
                navigationContentDescription = getString(R.string.accessibility_close)
                setNavigationOnClickListener { vm.onBackButtonPress() }
            }
        }

        vm.qrCodeBitmap.observe2(this) {
            binding.qrCodeImage.setImageBitmap(it)
        }

        vm.routeToScreen.observe2(this) {
            when (it) {
                QrCodeDetailNavigationEvents.NavigateBack -> {
                    popBackStack()
                }
                QrCodeDetailNavigationEvents.NavigateToPrintFragment -> { /* TODO */ }
                QrCodeDetailNavigationEvents.NavigateToDuplicateFragment -> { /* TODO */ }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
