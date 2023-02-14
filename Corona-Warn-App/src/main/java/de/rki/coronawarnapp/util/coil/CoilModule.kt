package de.rki.coronawarnapp.util.coil

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.qrcode.coil.BitMatrixDecoder
import de.rki.coronawarnapp.util.qrcode.coil.QrCodeBitMatrixFetcher
import timber.log.Timber
import javax.inject.Provider
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CoilModule {

    @Provides
    fun imageLoader(
        @AppContext context: Context,
        qrCodeBitMatrixFetcher: QrCodeBitMatrixFetcher,
        bitMatrixDecoder: BitMatrixDecoder,
    ): ImageLoader = ImageLoader.Builder(context).apply {
        if (CWADebug.isDebugBuildOrMode) {
            val logger = object : Logger {
                override var level: Int = when {
                    CWADebug.isDeviceForTestersBuild -> Log.VERBOSE
                    else -> Log.INFO
                }

                override fun log(tag: String, priority: Int, message: String?, throwable: Throwable?) {
                    Timber.tag("Coil:$tag").log(priority, throwable, message)
                }
            }
            logger(logger)
        }
        componentRegistry {
            add(qrCodeBitMatrixFetcher)
            add(bitMatrixDecoder)
        }
    }.build()

    @Singleton
    @Provides
    fun imageLoaderFactory(imageLoaderSource: Provider<ImageLoader>): ImageLoaderFactory = ImageLoaderFactory {
        Timber.i("Creating ImageLoader...")
        imageLoaderSource.get()
    }
}
