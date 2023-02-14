package de.rki.coronawarnapp.miscinfo

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.asLiveData
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MiscInfoFragmentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val enfClient: ENFClient,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val errorEvent = SingleLiveEvent<String>()

    val versionState = flow {
        val enfVersion = enfClient.getENFClientVersion()

        val gmsVersion = try {
            PackageInfoCompat.getLongVersionCode(
                context.packageManager.getPackageInfo(
                    GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                    0
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get GMS PackageInfo")
            errorEvent.postValue(e.toString())
            null
        }

        emit(
            GoogleServicesState(gmsVersion = gmsVersion, enfVersion = enfVersion).also {
                Timber.i("Google Service Infos: %s", it)
            }
        )
    }.asLiveData(context = dispatcherProvider.Default)
}
