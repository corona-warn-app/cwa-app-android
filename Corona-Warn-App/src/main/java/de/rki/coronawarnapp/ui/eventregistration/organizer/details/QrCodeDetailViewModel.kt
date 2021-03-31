package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class QrCodeDetailViewModel @AssistedInject constructor(
    private val dispatcher: DispatcherProvider,
    private val qrCodeGenerator: QrCodeGenerator,
) : CWAViewModel() {

    /* Currently hardcoded to show the functionality -> will be connected to the database in a separate PR */
    val qrCodeText = "HTTPS://E.CORONAWARN.APP/C1/BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUD" +
        "BOJ2HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGCPUZ2RQACAYEJ3HQYMAFF" +
        "BU2SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU7TYERH23B746RQTABO3CTI="
    val titleText = "Jahrestreffen der deutschen SAP Anwendergruppe"
    val subtitleText = "Hauptstr 3, 69115 Heidelberg"
    val eventDate = "21.01.2021, 18:00 - 21:00 Uhr"

    init {
        createQrCode()
    }

    private val bitmapLiveData = MutableLiveData<Bitmap>()
    val qrCodeBitmap: LiveData<Bitmap> = bitmapLiveData
    val routeToScreen: SingleLiveEvent<QrCodeDetailNavigationEvents> = SingleLiveEvent()

    /**
     * Creates a QR Code [Bitmap] ,result is delivered by [qrCodeBitmap]
     */
    private fun createQrCode() = launch(context = dispatcher.IO) {
        try {
            bitmapLiveData.postValue(qrCodeGenerator.createQrCode(qrCodeText))
        } catch (e: Exception) {
            Timber.d(e, "Qr code creation failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(QrCodeDetailNavigationEvents.NavigateBack)
    }

    fun onPrintQrCode() {
        routeToScreen.postValue(QrCodeDetailNavigationEvents.NavigateToPrintFragment(qrCodeText))
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeDetailViewModel>
}
