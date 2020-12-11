package de.rki.coronawarnapp.contactdiary.ui.onboarding

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryOnboardingFragmentModul {

    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryOnboardingFragmentViewModel::class)
    abstract fun contactDiaryOnboardingFragmentVM(
        factory: ContactDiaryOnboardingFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun contactDiaryOnboardingFragmentVM(): ContactDiaryOnboardingFragment
}
