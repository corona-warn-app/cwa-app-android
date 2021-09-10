package de.rki.coronawarnapp.qrcode

import android.os.Build
import com.google.zxing.qrcode.QRCodeReader
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.qrcode.provider.image.BaseImageUriResolver
import de.rki.coronawarnapp.qrcode.provider.image.ImageUriResolver
import de.rki.coronawarnapp.qrcode.provider.image.NewImageUriResolver
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanFragment
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanModule
import javax.inject.Singleton

@Module
class QrCodeScannerModule {
    @Provides
    @Singleton
    fun bindImageResolver(
        baseImageUriResolver: BaseImageUriResolver,
        newImageUriResolver: NewImageUriResolver
    ): ImageUriResolver =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) newImageUriResolver else baseImageUriResolver

    @Provides
    @Singleton
    fun qrCodeReader(): QRCodeReader = QRCodeReader()
}
