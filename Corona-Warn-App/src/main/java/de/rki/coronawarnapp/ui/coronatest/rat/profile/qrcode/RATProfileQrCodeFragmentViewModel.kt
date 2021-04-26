package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RATProfileQrCodeFragmentViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettings,
    private val qrCodeGenerator: QrCodeGenerator,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {

    private val qrCodeImageDate = MutableLiveData<Bitmap>()
    val qrCodeImage: LiveData<Bitmap> = qrCodeImageDate
    val profile: LiveData<RATProfile?> = ratProfileSettings.profile.flow.map { profile ->
        profile?.let { generateQrCode(it) }
            .run { profile }
    }.asLiveData(context = dispatcherProvider.Default)

    fun deleteProfile() {
        ratProfileSettings.deleteProfile()
    }

    private suspend fun generateQrCode(ratProfile: RATProfile) {
        try {
            qrCodeImageDate.postValue(
                qrCodeGenerator.createQrCode(ratProfile.toString())
            )
        } catch (e: Exception) {
            Timber.d(e, "Failed to generate profile Qr Code")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATProfileQrCodeFragmentViewModel>
}
