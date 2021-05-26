package de.rki.coronawarnapp.ui.presencetracing.organizer.details

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerQrCodeDetailFragmentBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class QrCodeDetailFragment : Fragment(R.layout.trace_location_organizer_qr_code_detail_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val navArgs by navArgs<QrCodeDetailFragmentArgs>()

    private val viewModel: QrCodeDetailViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as QrCodeDetailViewModel.Factory
            factory.create(navArgs.traceLocationId)
        }
    )

    private val binding: TraceLocationOrganizerQrCodeDetailFragmentBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform()
        sharedElementReturnTransition = MaterialContainerTransform()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarOverlay()

        binding.apply {
            appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                title.alpha = titleAlpha
                subtitle.alpha = subtitleAlpha
                checkShadowVisibility()
            }
            root.viewTreeObserver.addOnGlobalLayoutListener { checkShadowVisibility() }

            toolbar.apply {
                navigationIcon = context.getDrawableCompat(R.drawable.ic_close_white)
                navigationContentDescription = getString(R.string.accessibility_close)
                setNavigationOnClickListener { viewModel.onBackButtonPress() }
            }

            qrCodePrintButton.setOnClickListener {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

                viewModel.onPrintQrCode()
            }

            qrCodeCloneButton.setOnClickListener {
                viewModel.duplicateTraceLocation()
            }

            root.transitionName = navArgs.traceLocationId.toString()

            qrCodeImage.setOnClickListener {
                viewModel.openFullScreen()
            }
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                QrCodeDetailNavigationEvents.NavigateBack -> popBackStack()

                is QrCodeDetailNavigationEvents.NavigateToDuplicateFragment -> doNavigate(
                    QrCodeDetailFragmentDirections.actionQrCodeDetailFragmentToTraceLocationCreateFragment(
                        it.category,
                        it.traceLocation
                    )
                )

                is QrCodeDetailNavigationEvents.NavigateToQrCodePosterFragment -> doNavigate(
                    QrCodeDetailFragmentDirections.actionQrCodeDetailFragmentToQrCodePosterFragment(it.locationId)
                )
                is QrCodeDetailNavigationEvents.NavigateToFullScreenQrCode -> findNavController().navigate(
                    R.id.action_global_qrCodeFullScreenFragment,
                    QrCodeFullScreenFragmentArgs(it.qrcodeText, it.correctionLevel).toBundle(),
                    null,
                    FragmentNavigatorExtras(binding.qrCodeImage to binding.qrCodeImage.transitionName)
                )
            }
        }

        viewModel.uiState.observe2(this) { uiState ->
            with(binding) {
                title.text = uiState.description
                subtitle.text = uiState.address

                if (uiState.startDateTime != null && uiState.endDateTime != null) {

                    val startTime = uiState.startDateTime!!.toDateTime()
                    val endTime = uiState.endDateTime!!.toDateTime()

                    eventDate.isGone = false

                    val startDay = startTime.toLocalDate().toString("dd.MM.yyyy")
                    val startHour = startTime.toLocalTime().toString("HH:mm")
                    val endDay = endTime.toLocalDate().toString("dd.MM.yyyy")
                    val endHour = endTime.toLocalTime().toString("HH:mm")
                    eventDate.text = if (startTime.toLocalDate() == endTime.toLocalDate()) {
                        requireContext().getString(
                            R.string.trace_location_organizer_detail_item_duration,
                            startDay,
                            startHour,
                            endHour
                        )
                    } else {
                        requireContext().getString(
                            R.string.trace_location_organizer_detail_item_duration_multiple_days,
                            startDay,
                            startHour,
                            endDay,
                            endHour
                        )
                    }
                } else {
                    eventDate.isGone = true
                }

                binding.progressBar.hide()
                binding.qrCodeImage.setImageBitmap(uiState.bitmap)
            }
        }
    }

    private fun TraceLocationOrganizerQrCodeDetailFragmentBinding.checkShadowVisibility() {
        shadowView.isInvisible = nestedScrollView.bottom <= shadowView.y
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
