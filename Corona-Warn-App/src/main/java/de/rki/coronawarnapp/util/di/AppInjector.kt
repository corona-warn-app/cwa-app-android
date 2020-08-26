package de.rki.coronawarnapp.util.di

import de.rki.coronawarnapp.CoronaWarnApplication

object AppInjector {
    fun init(app: CoronaWarnApplication) {
        DaggerApplicationComponent.factory()
            .create(app)
            .inject(app)
    }
}
