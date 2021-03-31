package de.rki.coronawarnapp.ui.eventregistration.organizer.poster

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.PosterImageProvider
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.Template
import de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate.QrCodePosterTemplateServer
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.CheckInsViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class QrCodePosterViewModel @AssistedInject constructor(
    @Assisted private val locationID: String,
    private val dispatcher: DispatcherProvider,
    private val qrCodeGenerator: QrCodeGenerator,
    private val posterImageProvider: PosterImageProvider

) : CWAViewModel(dispatcher) {

    private val posterLiveData = MutableLiveData<Poster>()
    val poster: LiveData<Poster> = posterLiveData

    init {
        generatePoster()
    }

    private fun generatePoster() = launch(context = dispatcher.IO) {
        try {
            val template = posterImageProvider.posterTemplate()
            val qrCode = qrCodeGenerator.createQrCode(locationID, 700)

            posterLiveData.postValue(
                Poster(qrCode = qrCode, template = template)
            )
        } catch (e: Exception) {
            Timber.d(e, "Generating poster failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodePosterViewModel> {
        fun create(
            locationID: String
        ): QrCodePosterViewModel
    }
}

data class Poster(val qrCode: Bitmap?, val template: Template)
