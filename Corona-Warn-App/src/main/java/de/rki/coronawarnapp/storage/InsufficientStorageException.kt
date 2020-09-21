package de.rki.coronawarnapp.storage

import java.io.IOException

class InsufficientStorageException(
    val result: DeviceStorage.CheckResult
) : IOException("Not enough free space (Want:${result.requiredBytes}; Have:${result.freeBytes}")
