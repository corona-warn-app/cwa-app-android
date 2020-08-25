package de.rki.coronawarnapp.util.storage

import android.os.StatFs
import timber.log.Timber
import java.io.File

// TODO Add inject when #1069 is merged
class StatsFsProvider {

    fun createStats(path: File): StatFs {
        Timber.tag(TAG).v("createStats(path=%s)", path)
        return StatFs(path.path)
    }

    companion object {
        val TAG = StatsFsProvider::class.java.simpleName
    }
}
