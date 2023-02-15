package de.rki.coronawarnapp.test.eol

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eol.EolSetting
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import java.time.ZonedDateTime

class EolTestViewModel @AssistedInject constructor(
    private val eolSetting: EolSetting
) : CWAViewModel() {

    val dateTime = eolSetting.eolDateTime.asLiveData2()

    fun updateEolDateTime(dateTime: ZonedDateTime) = launch {
        eolSetting.setEolDateTime(dateTime.toString())
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EolTestViewModel>
}
