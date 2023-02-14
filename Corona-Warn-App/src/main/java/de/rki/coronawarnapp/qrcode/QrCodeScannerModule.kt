package de.rki.coronawarnapp.qrcode

import android.os.Build
import com.google.zxing.qrcode.QRCodeReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.qrcode.provider.image.BaseImageUriResolver
import de.rki.coronawarnapp.qrcode.provider.image.ImageUriResolver
import de.rki.coronawarnapp.qrcode.provider.image.NewImageUriResolver
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class QrCodeScannerModule {
    @Provides
    @Singleton
    fun bindImageResolver(
        baseImageUriResolver: BaseImageUriResolver,
        newImageUriResolver: NewImageUriResolver
    ): ImageUriResolver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        newImageUriResolver
    else
        baseImageUriResolver

    @Provides
    @Singleton
    fun qrCodeReader(): QRCodeReader = QRCodeReader()
}
