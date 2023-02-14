package de.rki.coronawarnapp.bugreporting.debuglog

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EntryPoint
interface DebugEntryPoint{
    fun inject(debugLogger: DebugLogger)
}
