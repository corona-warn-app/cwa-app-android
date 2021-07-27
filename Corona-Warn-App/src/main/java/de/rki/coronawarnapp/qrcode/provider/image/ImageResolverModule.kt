package de.rki.coronawarnapp.qrcode.provider.image

import android.os.Build
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ImageResolverModule {
    @Provides
    @Singleton
    fun bindImageResolver(
        baseImageUriResolver: BaseImageUriResolver,
        newImageUriResolver: NewImageUriResolver
    ): ImageUriResolver =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) newImageUriResolver else baseImageUriResolver
}
