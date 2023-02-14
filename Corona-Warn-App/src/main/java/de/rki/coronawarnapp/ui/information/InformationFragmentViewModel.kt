package de.rki.coronawarnapp.ui.information

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class InformationFragmentViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    enfClient: ENFClient,
    @ApplicationContext private val context: Context,
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
        val enfVersion = enfClient.getENFClientVersion()?.let { v ->
            val vString = v.toString()
            val version = "v%s.%s (%s)".format(
                vString.getOrElse(0) { '0' },
                vString.getOrElse(1) { '0' },
                vString
            )
            /**
             * Note for Devs: Google official abbreviation is ENS (Exposure Notifications System)
             * while for some historic reasons it is referred to as ENF (Exposure Notifications Framework)
             * in the code see [ENFClient] and [ENFVersion]
             */
            "ENS ${context.getString(R.string.information_version).format(version)}"
        }
        emit(enfVersion)
    }.asLiveData(context = dispatcherProvider.Default)

    val appVersion = flowOf(
        context.getString(R.string.information_version).format(BuildConfig.VERSION_NAME)
    ).asLiveData(context = dispatcherProvider.Default)
}
