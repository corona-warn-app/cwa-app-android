package de.rki.coronawarnapp.diagnosiskeys.storage

import java.io.File

data class CachedKey(val info: CachedKeyInfo, val path: File)
