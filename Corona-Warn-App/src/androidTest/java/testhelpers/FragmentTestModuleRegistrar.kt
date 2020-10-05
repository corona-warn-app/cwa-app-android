package testhelpers

import dagger.Module
import de.rki.coronawarnapp.ui.main.home.HomeFragmentTestModule

@Module(
    includes = [
        HomeFragmentTestModule::class
    ]
)
class FragmentTestModuleRegistrar
