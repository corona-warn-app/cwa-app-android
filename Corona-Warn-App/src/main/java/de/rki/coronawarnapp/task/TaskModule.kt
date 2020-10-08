package de.rki.coronawarnapp.task

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.task.example.ExampleTaskModule
import de.rki.coronawarnapp.transaction.TransactionCoroutineScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module(
    includes = [
        ExampleTaskModule::class]
)
class TaskModule {

    @Provides
    @Singleton
    @TaskCoroutineScope
    fun provideScope(scope: TransactionCoroutineScope): CoroutineScope = scope
}
