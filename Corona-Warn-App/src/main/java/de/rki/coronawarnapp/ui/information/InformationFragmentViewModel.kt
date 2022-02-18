package de.rki.coronawarnapp.ui.information

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@SuppressLint("StaticFieldLeak")
class InformationFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    enfClient: ENFClient,
    @AppContext private val context: Context,
    cclConfigurationRepository: CclConfigurationRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val cclConfigVersion = cclConfigurationRepository.cclConfigurations.map { configs ->
        val version = configs
            .sortedBy { it.identifier }
            .map { it.version }
            .toSet()
            .joinToString(", ")
        "CCL ${context.getString(R.string.information_version).format(version)}"
    }.asLiveData2()

    val currentENFVersion = flow {
        val enfVersion = enfClient.getENFClientVersion()
            ?.let { "ENF ${context.getString(R.string.information_version).format(it)}" }
        emit(enfVersion)
    }.asLiveData(context = dispatcherProvider.Default)

    val appVersion = flowOf(
        context.getString(R.string.information_version).format(BuildConfig.VERSION_NAME)
    ).asLiveData(context = dispatcherProvider.Default)

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<InformationFragmentViewModel>
}
