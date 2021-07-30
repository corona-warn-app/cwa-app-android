package de.rki.coronawarnapp.test.dsc.ui

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.Instant

class DscViewModel @AssistedInject constructor(
    private val dscRepository: DscRepository
) : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DscViewModel>

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
            dscRepository.refresh()
        }
    }

    data class DscDataInfo(val lastUpdate: String, val listSize: Int)
}
