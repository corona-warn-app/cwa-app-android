package de.rki.coronawarnapp.receiver

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.notification.NotificationReceiver
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOutBootRestoreReceiver
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOutReceiver

@Module
internal abstract class ReceiverBinder {

    @ContributesAndroidInjector
    internal abstract fun exposureUpdateReceiver(): ExposureStateUpdateReceiver

    @ContributesAndroidInjector
    internal abstract fun notificationReceiver(): NotificationReceiver

    @ContributesAndroidInjector
    internal abstract fun autoCheckOutRestore(): AutoCheckOutBootRestoreReceiver

    @ContributesAndroidInjector
    internal abstract fun autoCheckOutTrigger(): AutoCheckOutReceiver
}
