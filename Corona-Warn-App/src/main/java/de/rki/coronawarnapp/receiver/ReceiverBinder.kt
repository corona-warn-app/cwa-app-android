package de.rki.coronawarnapp.receiver

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ReceiverBinder {

    @ContributesAndroidInjector
    internal abstract fun exposureUpdateReceiver(): ExposureStateUpdateReceiver
}
