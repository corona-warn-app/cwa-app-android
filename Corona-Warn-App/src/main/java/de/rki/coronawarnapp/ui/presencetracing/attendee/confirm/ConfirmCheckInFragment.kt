package de.rki.coronawarnapp.ui.presencetracing.attendee.confirm

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentConfirmCheckInBinding
import de.rki.coronawarnapp.ui.durationpicker.DurationPicker
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.Duration
import javax.inject.Inject
import kotlin.math.abs

class ConfirmCheckInFragment : Fragment(R.layout.fragment_confirm_check_in), AutoInject {
    private val navArgs by navArgs<ConfirmCheckInFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ConfirmCheckInViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ConfirmCheckInViewModel.Factory
            factory.create(navArgs.verifiedTraceLocation)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    private val binding: FragmentConfirmCheckInBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            appBarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    title.alpha = (
                        1.0f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.6f))
                        )
                }
            )

            toolbar.setNavigationOnClickListener { viewModel.onClose() }

            confirmCheckinSettingsCardCheckoutToggle.setOnCheckedChangeListener { _, isChecked ->
                viewModel.createJournalEntryToggled(isChecked)
            }

            confirmCheckinSettingsCardCheckoutTimeRow.setOnClickListener {
                viewModel.dateSelectorClicked()
            }

            confirmCheckinConfirmButton.setOnClickListener {
                viewModel.onConfirmTraceLocation()
            }
        }

        viewModel.events.observe2(this) { navEvent ->
            when (navEvent) {
                ConfirmCheckInNavigation.BackNavigation -> popBackStack()
                ConfirmCheckInNavigation.ConfirmNavigation -> popBackStack()
            }
        }

        viewModel.uiState.observe2(this) { uiState ->
            with(binding) {
                confirmCheckinInfoCardHeader.text = getString(uiState.typeRes)
                confirmCheckinInfoCardTitle.text = uiState.description
                confirmCheckinInfoCardAddress.text = uiState.address
                confirmCheckinSettingsCardCheckoutToggle.isChecked = uiState.createJournalEntry
                confirmCheckinSettingsCardCheckoutTime.text = uiState.checkInEnd

                confirmCheckinEventInPastCard.isGone = !uiState.eventInPastVisible

                confirmCheckinEventInFutureCard.isGone = !uiState.eventInFutureVisible
                confirmCheckinEventInFutureCardText.text = getString(
                    R.string.confirm_checkin_event_in_future_card_text,
                    uiState.eventInFutureDateText,
                    uiState.eventInFutureTimeText
                )
                confirmCheckinConfirmButton.isEnabled = uiState.confirmButtonEnabled
            }
        }

        viewModel.openDatePickerEvent.observe2(this) { time ->
            showDurationPicker(time) {
                viewModel.durationUpdated(it)
            }
        }
    }

    private fun showDurationPicker(
        defaultValue: String?,
        callback: (Duration) -> Unit
    ) {
        val durationPicker = DurationPicker.Builder()
            .duration(defaultValue ?: "00:00")
            .title(getString(R.string.duration_dialog_title))
            .build()
        durationPicker.show(parentFragmentManager, DURATION_PICKER_TAG)
        durationPicker.setDurationChangeListener(callback)
    }

    companion object {
        private const val DURATION_PICKER_TAG = "duration_picker"
    }
}
