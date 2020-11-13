package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import java.io.File

interface DiagnosisKeyProvider {

    suspend fun provideDiagnosisKeys(keyFiles: Collection<File>): Boolean
}
