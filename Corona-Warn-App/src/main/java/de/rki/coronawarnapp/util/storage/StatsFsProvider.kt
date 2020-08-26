package de.rki.coronawarnapp.util.storage

import android.os.StatFs
import dagger.Reusable
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@Reusable
class StatsFsProvider @Inject constructor() {

    fun createStats(path: File): StatFs {
        Timber.tag(TAG).v("createStats(path=%s)", path)
        return StatFs(path.path)
    }

    companion object {
        val TAG = StatsFsProvider::class.java.simpleName
    }
}
