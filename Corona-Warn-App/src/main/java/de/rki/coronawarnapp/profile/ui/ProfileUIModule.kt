package de.rki.coronawarnapp.profile.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.profile.ui.create.ProfileCreateFragment
import de.rki.coronawarnapp.profile.ui.create.ProfileCreateFragmentModule
import de.rki.coronawarnapp.profile.ui.list.ProfileListFragment
import de.rki.coronawarnapp.profile.ui.list.ProfileListFragmentModule
import de.rki.coronawarnapp.profile.ui.onboarding.ProfileOnboardingFragment
import de.rki.coronawarnapp.profile.ui.onboarding.ProfileOnboardingFragmentModule
import de.rki.coronawarnapp.profile.ui.qrcode.ProfileQrCodeFragment
import de.rki.coronawarnapp.profile.ui.qrcode.ProfileQrCodeFragmentModule

@Module
internal abstract class ProfileUIModule {

    @ContributesAndroidInjector(modules = [ProfileCreateFragmentModule::class])
    abstract fun profileCreateFragment(): ProfileCreateFragment

    @ContributesAndroidInjector(modules = [ProfileQrCodeFragmentModule::class])
    abstract fun profileQrCodeFragment(): ProfileQrCodeFragment

    @ContributesAndroidInjector(modules = [ProfileOnboardingFragmentModule::class])
    abstract fun profileOnboardingFragment(): ProfileOnboardingFragment

    @ContributesAndroidInjector(modules = [ProfileListFragmentModule::class])
    abstract fun profileListFragment(): ProfileListFragment
}
