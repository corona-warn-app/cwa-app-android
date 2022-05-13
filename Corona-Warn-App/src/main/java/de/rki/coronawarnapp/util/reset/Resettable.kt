package de.rki.coronawarnapp.util.reset

interface Resettable {
    suspend fun reset()
}
