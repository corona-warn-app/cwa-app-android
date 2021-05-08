package de.rki.coronawarnapp.ui.presencetracing.organizer.details

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerQrCodeDetailFragmentBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject
import kotlin.math.abs

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

    private val binding: TraceLocationOrganizerQrCodeDetailFragmentBinding by viewBindingLazy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform()
        sharedElementReturnTransition = MaterialContainerTransform()
    }

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

                    shadowView.isGone = nestedScrollView.bottom <= shadowView.y
                }
            )

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
                    QrCodeFullScreenFragmentArgs(it.qrcodeText).toBundle(),
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
                    eventDate.text = if (startTime.toLocalDate() == endTime.toLocalDate()) {
                        requireContext().getString(
                            R.string.trace_location_organizer_detail_item_duration,
                            startTime.toLocalDate().toString("dd.MM.yyyy"),
                            startTime.toLocalTime().toString("HH:mm"),
                            endTime.toLocalTime().toString("HH:mm")
                        )
                    } else {
                        requireContext().getString(
                            R.string.trace_location_organizer_detail_item_duration_multiple_days,
                            startTime.toLocalDate().toString("dd.MM.yyyy"),
                            startTime.toLocalTime().toString("HH:mm"),
                            endTime.toLocalDate().toString("dd.MM.yyyy"),
                            endTime.toLocalTime().toString("HH:mm")
                        )
                    }
                } else {
                    eventDate.isGone = true
                }

                uiState.bitmap?.let {
                    binding.progressBar.hide()
                    binding.qrCodeImage.apply {
                        val resourceId = RoundedBitmapDrawableFactory.create(resources, it)
                        resourceId.cornerRadius = 15f
                        setImageDrawable(resourceId)
                    }
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
