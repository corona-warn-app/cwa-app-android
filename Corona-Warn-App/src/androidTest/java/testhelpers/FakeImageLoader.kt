package testhelpers

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.SourceResult
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import de.rki.coronawarnapp.util.qrcode.coil.BitMatrixDecoder
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.qrcode.coil.QrCodeBitMatrixFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @param forcedQrCode pass one if you want to overwrite all resolved qrcodes to this one.
 */
fun createFakeImageLoaderForQrCodes(forcedQrCode: CoilQrCode? = null): suspend (ImageRequest) -> Drawable? {
    val fetcher = QrCodeBitMatrixFetcher()
    val pool = BitmapPool(0)

    return provider@{ request: ImageRequest ->
        if (request.data !is CoilQrCode) return@provider null

        val size = request.sizeResolver.size()
        val options = Options(context = request.context)

        val fetchResult = fetcher.fetch(
            pool = pool,
            data = forcedQrCode ?: request.data as CoilQrCode,
            size = size,
            options = options
        )

        val decodeResult = BitMatrixDecoder(request.context).decode(
            pool = pool,
            source = (fetchResult as SourceResult).source,
            size = size,
            options = options
        )
        decodeResult.drawable
    }
}

fun createFakeDrawableProvider(@DrawableRes drawableRes: Int): suspend (ImageRequest) -> Drawable? {
    val context: Context = ApplicationProvider.getApplicationContext()
    return provider@{
        if (it.data !is Int) return@provider null
        ContextCompat.getDrawable(context, drawableRes)!!
    }
}

fun setupFakeImageLoader(vararg drawableProvider: suspend (ImageRequest) -> Drawable?) {
    val context: Context = ApplicationProvider.getApplicationContext()
    val imageLoader = FakeImageLoader(context) { request ->
        for (provider in drawableProvider) {
            val drawable = provider(request)
            if (drawable != null) return@FakeImageLoader drawable
        }
        throw IllegalStateException("FakeImageLoader: No matching drawable provider for $request")
    }
    Coil.setImageLoader(imageLoader)
}

class FakeImageLoader(
    private val context: Context,
    private val drawableProvider: suspend (ImageRequest) -> Drawable,
) : ImageLoader {
    override val defaults = DefaultRequestOptions()

    override val memoryCache get() = throw UnsupportedOperationException()

    override val bitmapPool = BitmapPool(0)

    private suspend fun createResult(request: ImageRequest): ImageResult = SuccessResult(
        drawable = drawableProvider(request),
        request = request,
        metadata = ImageResult.Metadata(
            memoryCacheKey = null,
            isSampled = false,
            dataSource = DataSource.MEMORY_CACHE,
            isPlaceholderMemoryCacheKeyPresent = false
        )
    )

    private fun exerciseResult(result: ImageResult) {
        when (result) {
            is SuccessResult -> result.request.apply {
                target?.onStart(placeholder = placeholder)
                listener?.onStart(this)
                target?.onSuccess(result = result.drawable)
                listener?.onSuccess(this, result.metadata)
            }
            is ErrorResult -> throw NotImplementedError()
        }
    }

    override fun enqueue(request: ImageRequest): Disposable {
        GlobalScope.launch(context = Dispatchers.Main) {
            val result = createResult(request)
            exerciseResult(result)
        }
        return object : Disposable {
            override val isDisposed get() = true
            override fun dispose() {}
            override suspend fun await() {}
        }
    }

    override suspend fun execute(request: ImageRequest): ImageResult = createResult(request).also {
        exerciseResult(it)
    }

    override fun shutdown() {}

    override fun newBuilder() = ImageLoader.Builder(context)
}
