package de.rki.coronawarnapp.receiver

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.notification.NotificationReceiver

@Module
internal abstract class ReceiverBinder {

    @ContributesAndroidInjector
    internal abstract fun exposureUpdateReceiver(): ExposureStateUpdateReceiver
    @ContributesAndroidInjector
    internal abstract fun notificationReceiver(): NotificationReceiver
}
