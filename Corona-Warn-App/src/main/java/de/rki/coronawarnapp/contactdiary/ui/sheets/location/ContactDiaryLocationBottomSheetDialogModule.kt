package de.rki.coronawarnapp.contactdiary.ui.sheets.location

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryLocationBottomSheetDialogModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryLocationBottomSheetDialogViewModel::class)
    abstract fun contactDiaryLocationBottomSheetDialogFragment(
        factory: ContactDiaryLocationBottomSheetDialogViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
