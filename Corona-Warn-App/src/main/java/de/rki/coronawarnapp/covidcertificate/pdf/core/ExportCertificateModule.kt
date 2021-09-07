package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.di.AppContext
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class ExportCertificateModule {

    @Singleton
    @Provides
    @OpenSansTypeFace
    fun provideFont(
        @AppContext context: Context
    ): Typeface = ResourcesCompat.getFont(context, R.font.opensans)!!

    @Singleton
    @Provides
    @CertificateExportCache
    fun cacheDir(
        @AppContext context: Context
    ): File = File(context.cacheDir, "export")
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CertificateExportCache

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenSansTypeFace
