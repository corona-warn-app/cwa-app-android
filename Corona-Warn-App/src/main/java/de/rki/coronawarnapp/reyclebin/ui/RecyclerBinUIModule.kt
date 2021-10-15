package de.rki.coronawarnapp.reyclebin.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class RecyclerBinUIModule {

    @ContributesAndroidInjector(modules = [RecyclerBinOverviewFragmentModule::class])
    abstract fun recyclerBinOverviewFragment(): RecyclerBinOverviewFragment
}
