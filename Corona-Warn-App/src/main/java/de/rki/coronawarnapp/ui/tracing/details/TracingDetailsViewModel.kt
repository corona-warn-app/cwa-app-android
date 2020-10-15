package de.rki.coronawarnapp.ui.tracing.details

import androidx.lifecycle.LiveData
import de.rki.coronawarnapp.tracing.TracingStatus
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

class TracingDetailsViewModel @Inject constructor(
    private val tracingStatus: TracingStatus
) : CWAViewModel() {

    val state: LiveData<TracingDetailsState> = TODO()
}
