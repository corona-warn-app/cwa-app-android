package de.rki.coronawarnapp.ui.coronatest.rat.profile

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.coronatest.rat.profile.create.RATProfileCreateFragment
import de.rki.coronawarnapp.ui.coronatest.rat.profile.create.RATProfileCreateFragmentModule

@Module
internal abstract class RATProfileUIModule {

    @ContributesAndroidInjector(modules = [RATProfileCreateFragmentModule::class])
    abstract fun ratProfileCreateFragment(): RATProfileCreateFragment
}
