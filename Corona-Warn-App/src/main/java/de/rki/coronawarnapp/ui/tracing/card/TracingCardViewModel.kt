package de.rki.coronawarnapp.ui.tracing.card

import androidx.lifecycle.LiveData
import de.rki.coronawarnapp.tracing.TracingStatus
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

class TracingCardViewModel @Inject constructor(
    private val tracingStatus: TracingStatus
) : CWAViewModel() {

    val state: LiveData<TracingCardState> = TODO()
}
