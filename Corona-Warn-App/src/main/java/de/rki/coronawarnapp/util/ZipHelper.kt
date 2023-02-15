package de.rki.coronawarnapp.util

import timber.log.Timber
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipHelper {
    fun InputStream.unzip(): Sequence<Pair<ZipEntry, InputStream>> = sequence {
        ZipInputStream(this@unzip).use {
            do {
                val entry = it.nextEntry
                if (entry != null) {
                    Timber.v("Reading zip entry ${entry.name}")
                    yield(entry to it)
                    it.closeEntry()
                }
            } while (entry != null)
        }
    }

    fun Sequence<Pair<ZipEntry, InputStream>>.readIntoMap() =
        fold(emptyMap()) { last: Map<String, ByteArray>, (entry, stream) ->
            last.plus(entry.name to stream.readBytes())
        }
}
