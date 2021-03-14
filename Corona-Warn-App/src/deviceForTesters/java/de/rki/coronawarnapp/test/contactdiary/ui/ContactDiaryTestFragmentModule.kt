package de.rki.coronawarnapp.test.contactdiary.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryTestFragmentViewModel::class)
    abstract fun testContactDiaryFragment(
        factory: ContactDiaryTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
