package de.rki.coronawarnapp.test.dsc.ui

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import java.lang.Exception

class DscTestViewModel @AssistedInject constructor(
    private val dscRepository: DscRepository
) : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DscTestViewModel>

    val errorEvent = SingleLiveEvent<Unit>()

    val dscData: LiveData<DscDataInfo> = dscRepository.dscData.map {
        DscDataInfo(
            lastUpdate = if (it.updatedAt == Instant.EPOCH)
                "NEVER (using default_dsc_list)"
            else
                it.updatedAt.toString(),
            listSize = it.dscList.size
        )
    }.asLiveData2()

    fun clear() {
        launch {
            dscRepository.clear()
        }
    }

    fun refresh() {
        launch {
            try {
                dscRepository.refresh()
            } catch (e:Exception) {
                errorEvent.postValue(Unit)
            }
        }
    }

    data class DscDataInfo(val lastUpdate: String, val listSize: Int)
}
