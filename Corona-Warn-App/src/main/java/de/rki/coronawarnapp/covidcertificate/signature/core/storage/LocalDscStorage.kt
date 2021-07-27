package de.rki.coronawarnapp.covidcertificate.signature.core.storage

import de.rki.coronawarnapp.covidcertificate.signature.core.DscData
import de.rki.coronawarnapp.covidcertificate.signature.core.DscDataParser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDscStorage @Inject constructor(
    private val dscDataParser: DscDataParser,
) {
    private val mutex = Mutex()

    suspend fun load(): DscData = mutex.withLock {
        Timber.d("load()")
        throw NotImplementedError()
    }

    suspend fun save(rawData: ByteArray?): Unit = mutex.withLock {
        Timber.d("save(rawData=${rawData?.size}")
        throw NotImplementedError()
    }
}
