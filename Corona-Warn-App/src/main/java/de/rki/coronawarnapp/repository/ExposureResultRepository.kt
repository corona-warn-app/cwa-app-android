package de.rki.coronawarnapp.repository

import de.rki.coronawarnapp.fileio.CWAFileIO
import de.rki.coronawarnapp.risk.ExposureResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureResultRepository @Inject constructor(fileio: CWAFileIO) : FileRepository() {

    private val file = fileio.exposureResultFile

    fun load(): ExposureResult? = loadOrNull(file)

    fun upsert(exposureResult: ExposureResult?) = writeOrDelete(exposureResult, file)
}
