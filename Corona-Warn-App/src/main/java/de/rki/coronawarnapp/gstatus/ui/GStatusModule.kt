package de.rki.coronawarnapp.gstatus.ui

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.covidcertificate.pdf.core.ExportCertificateModule
import de.rki.coronawarnapp.covidcertificate.signature.core.server.DscServerModule
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateServerModule
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationModule
import de.rki.coronawarnapp.covidcertificate.valueset.CertificateValueSetModule
import de.rki.coronawarnapp.gstatus.notification.GStatusNotificationService

@Module
object GStatusModule {

    @IntoSet
    @Provides
    fun gStatusNotificationService(
        service: GStatusNotificationService
    ): DccWalletInfoNotificationService = service
}
