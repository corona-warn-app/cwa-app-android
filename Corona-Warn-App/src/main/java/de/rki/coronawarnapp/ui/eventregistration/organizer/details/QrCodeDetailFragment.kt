package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
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
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.DateTime
import java.time.Instant
import javax.inject.Inject
import kotlin.math.abs

class QrCodeDetailFragment : Fragment(R.layout.trace_location_organizer_qr_code_detail_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val navArgs by navArgs<QrCodeDetailFragmentArgs>()

    private val vm: QrCodeDetailViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as QrCodeDetailViewModel.Factory
            factory.create(if (navArgs.traceLocationId == "") null else navArgs.traceLocationId)
        }
    )

    private val binding: TraceLocationOrganizerQrCodeDetailFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarOverlay()

        binding.apply {
            appBarLayout.addOnOffsetChangedListener(
                OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    title.alpha = (
                        1.0f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.5f))
                        )
                    subtitle.alpha = (
                        1.0f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.7f))
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
            binding.qrCodeImage.apply {
                val resourceId = RoundedBitmapDrawableFactory.create(resources, it)
                resourceId.cornerRadius = it.width * 0.1f
                setImageDrawable(resourceId)
            }
        }

        vm.routeToScreen.observe2(this) {
            when (it) {
                QrCodeDetailNavigationEvents.NavigateBack -> {
                    popBackStack()
                }
                QrCodeDetailNavigationEvents.NavigateToPrintFragment -> { /* TODO */
                }
                QrCodeDetailNavigationEvents.NavigateToDuplicateFragment -> { /* TODO */
                }
            }
        }

        vm.uiState.observe2(this) { uiState ->
            with(binding) {
                title.text = uiState.description
                subtitle.text = uiState.address

                if (uiState.startDateTime != null && uiState.endDateTime != null) {

                    val startTime = uiState.startDateTime!!.toDateTime()
                    val endTime = uiState.endDateTime!!.toDateTime()

                    eventDate.isGone = false
                    eventDate.text = if (startTime.toLocalDate() == endTime.toLocalDate()) {
                        requireContext().getString (
                            R.string.trace_location_organizer_detail_item_duration,
                            startTime.toLocalDate().toString("dd.MM.yy"),
                            startTime.toLocalTime().toString("HH:mm"),
                            endTime.toLocalTime().toString("HH:mm")
                        )
                    } else {
                        requireContext().getString (
                            R.string.trace_location_organizer_detail_item_duration_multiple_days,
                            startTime.toLocalDate().toString("dd.MM.yy"),
                            endTime.toLocalTime().toString("HH:mm"),
                            endTime.toLocalDate().toString("dd.MM.yy"),
                            endTime.toLocalTime().toString("HH:mm")
                        )
                    }
                } else {
                    eventDate.isGone = true
                }
            }
        }
    }

    private fun setToolbarOverlay() {
        val width = requireContext().resources.displayMetrics.widthPixels

        val params: CoordinatorLayout.LayoutParams = binding.nestedScrollView.layoutParams
            as (CoordinatorLayout.LayoutParams)

        val textParams = binding.subtitle.layoutParams as (LinearLayout.LayoutParams)
        textParams.bottomMargin = ((width) / 2) - 24 /* 24 is space between screen border and QrCode */
        binding.subtitle.requestLayout() /* 24 is space between screen border and QrCode */

        val behavior: AppBarLayout.ScrollingViewBehavior = params.behavior as ((AppBarLayout.ScrollingViewBehavior))
        behavior.overlayTop = ((width) / 2) - 24
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
