package de.rki.coronawarnapp.util.di

import de.rki.coronawarnapp.CoronaWarnApplication

object AppInjector {
    lateinit var component: ApplicationComponent

    fun init(app: CoronaWarnApplication) {
        component = DaggerApplicationComponent.factory().create(app)
        component.inject(app)
    }
}
