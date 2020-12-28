package de.rki.coronawarnapp.contactdiary.ui.sheets.person

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryPersonBottomSheetDialogModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryPersonBottomSheetDialogViewModel::class)
    abstract fun contactDiaryPersonBottomSheetDialogFragment(
        factory: ContactDiaryPersonBottomSheetDialogViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
