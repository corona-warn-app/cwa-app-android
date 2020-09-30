package de.rki.coronawarnapp.playbook

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PlaybookModule {

    @Singleton
    @Provides
    fun providePlaybook(defaultPlayBook: DefaultPlaybook): Playbook = defaultPlayBook
}
