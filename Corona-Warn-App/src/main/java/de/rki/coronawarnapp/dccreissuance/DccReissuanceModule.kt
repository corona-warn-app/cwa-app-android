package de.rki.coronawarnapp.dccreissuance

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServerModule
import de.rki.coronawarnapp.dccreissuance.notification.DccReissuanceNotificationService

@Module(includes = [DccReissuanceServerModule::class])
object DccReissuanceModule {

    @IntoSet
    @Provides
    fun dccReissuanceNotificationService(
        service: DccReissuanceNotificationService
    ): DccWalletInfoNotificationService = service
}
