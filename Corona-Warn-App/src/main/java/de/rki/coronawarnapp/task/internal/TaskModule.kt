package de.rki.coronawarnapp.task.internal

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.task.TaskCoroutineScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class TaskModule {

    @Provides
    @Singleton
    @TaskCoroutineScope
    fun provideScope(scope: DefaultTaskCoroutineScope): CoroutineScope = scope
}
