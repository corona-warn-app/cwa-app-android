package de.rki.coronawarnapp.test.qrcode.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.qrcode.QRCodeFileParser
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class QrCodeTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val qrCodeFileParser: QRCodeFileParser
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val _qrCodeContent = MutableLiveData("QR Code Content")
    val qrCodeContent: LiveData<String> = _qrCodeContent

    fun onFileSelected(contentURI: Uri) = launch {
        _qrCodeContent.postValue("Decoding....")
        when (val result = qrCodeFileParser.decodeQrCodeFile(contentURI)) {
            is QRCodeFileParser.QRCodeParseResult.Success -> _qrCodeContent.postValue(result.text)
            is QRCodeFileParser.QRCodeParseResult.Failure -> _qrCodeContent.postValue(result.exception.message)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeTestFragmentViewModel>
}
