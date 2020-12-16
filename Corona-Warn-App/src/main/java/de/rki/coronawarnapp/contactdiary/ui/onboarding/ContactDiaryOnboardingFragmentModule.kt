package de.rki.coronawarnapp.contactdiary.ui.onboarding

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryOnboardingFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryOnboardingFragmentViewModel::class)
    abstract fun contactDiaryOnboardingFragmentVM(
        factory: ContactDiaryOnboardingFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
