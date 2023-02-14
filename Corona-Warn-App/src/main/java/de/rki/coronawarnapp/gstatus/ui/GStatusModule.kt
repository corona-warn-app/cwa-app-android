package de.rki.coronawarnapp.gstatus.ui

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.gstatus.notification.GStatusNotificationService

@InstallIn(SingletonComponent::class)
@Module
object GStatusModule {

    @IntoSet
    @Provides
    fun gStatusNotificationService(
        service: GStatusNotificationService
    ): DccWalletInfoNotificationService = service
}
