package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.content.Context
import android.graphics.Typeface
import android.print.PrintAttributes
import androidx.core.content.res.ResourcesCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.R
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ExportCertificateModule {

    @Singleton
    @Provides
    @OpenSansTypeFace
    fun provideFont(
        @ApplicationContext context: Context
    ): Typeface = ResourcesCompat.getFont(context, R.font.opensans)!!

    @Singleton
    @Provides
    @CertificateExportCache
    fun cacheDir(
        @ApplicationContext context: Context
    ): File = File(context.cacheDir, "export")

    @Singleton
    @Provides
    fun printAttributes(): PrintAttributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
        .build()
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CertificateExportCache

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenSansTypeFace
