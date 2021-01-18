package testhelpers

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import testhelpers.viewmodels.MockViewModelModule
import javax.inject.Singleton

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        MockViewModelModule::class,
        FragmentTestModuleRegistrar::class,
        TestAndroidModule::class
    ]
)
@Singleton
interface TestAppComponent : AndroidInjector<TestApplication> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: TestApplication): TestAppComponent
    }
}
