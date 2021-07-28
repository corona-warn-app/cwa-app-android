package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.failed.DccValidationFailedViewModel
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.passed.DccValidationPassedFragment
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.passed.DccValidationPassedViewModel
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.failed.DccValidationFailedFragment
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.open.DccValidationOpenFragment
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.open.DccValidationOpenViewModel
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart.ValidationStartModule
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccValidationResultModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccValidationFailedViewModel::class)
    abstract fun dccValidationFailedFragment(
        factory: DccValidationFailedViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector(modules = [ValidationStartModule::class])
    abstract fun validationResultOpenFragment(): DccValidationOpenFragment

    @Binds
    @IntoMap
    @CWAViewModelKey(DccValidationOpenViewModel::class)
    abstract fun dccValidationOpenFragment(
        factory: DccValidationOpenViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector(modules = [ValidationStartModule::class])
    abstract fun validationResultFailedFragment(): DccValidationFailedFragment

    @Binds
    @IntoMap
    @CWAViewModelKey(DccValidationPassedViewModel::class)
    abstract fun dccValidationPassedFragmentVM(
        factory: DccValidationPassedViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector(modules = [ValidationStartModule::class])
    abstract fun dccValidationPassedFragment(): DccValidationPassedFragment
}
