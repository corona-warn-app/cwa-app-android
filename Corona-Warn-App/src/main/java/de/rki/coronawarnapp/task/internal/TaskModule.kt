package de.rki.coronawarnapp.task.internal

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.task.TaskCoroutineScope
import de.rki.coronawarnapp.task.example.QueueingTaskModule
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module(
    includes = [
        QueueingTaskModule::class]
)
class TaskModule {

    @Provides
    @Singleton
    @TaskCoroutineScope
    fun provideScope(scope: DefaultTaskCoroutineScope): CoroutineScope = scope
}
