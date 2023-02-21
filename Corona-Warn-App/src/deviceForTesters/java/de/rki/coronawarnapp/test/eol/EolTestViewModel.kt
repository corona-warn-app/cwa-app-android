package de.rki.coronawarnapp.test.eol

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.eol.EolSetting
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class EolTestViewModel @Inject constructor(
    private val eolSetting: EolSetting
) : CWAViewModel() {

    val dateTime = eolSetting.eolDateTime.asLiveData2()
    val restart = SingleLiveEvent<Unit>()

    fun updateEolDateTime(dateTime: ZonedDateTime) = launch {
        eolSetting.setEolDateTime(dateTime.toString())
        restart.postValue(Unit)
    }

}
