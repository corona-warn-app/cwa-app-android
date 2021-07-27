package de.rki.coronawarnapp.qrcode

import com.google.zxing.qrcode.QRCodeReader
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class QRCodeParsingModule {
    @Provides
    @Singleton
    fun bindQrCodeReader(): QRCodeReader = QRCodeReader()
}
