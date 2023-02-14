package de.rki.coronawarnapp.dccreissuance

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServerModule
import de.rki.coronawarnapp.dccreissuance.notification.DccReissuanceNotificationService
@InstallIn(SingletonComponent::class)
@Module(includes = [DccReissuanceServerModule::class])
object DccReissuanceModule {

    @IntoSet
    @Provides
    fun dccReissuanceNotificationService(
        service: DccReissuanceNotificationService
    ): DccWalletInfoNotificationService = service
}
