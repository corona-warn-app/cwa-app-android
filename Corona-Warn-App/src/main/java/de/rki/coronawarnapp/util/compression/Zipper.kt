package de.rki.coronawarnapp.util.compression

import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Zipper(private val zipPath: File) {

    fun zip(toZip: List<Entry>) {
        if (zipPath.exists()) throw IOException("$zipPath already exists")

        Timber.tag(TAG).d("Creating ZIP file: %s", zipPath)
        zipPath.parentFile?.mkdirs()
        zipPath.createNewFile()

        if (!zipPath.exists()) throw IOException("Could not create $zipPath")

        ZipOutputStream(zipPath.outputStream().buffered()).use { output ->
            for (i in toZip.indices) {
                Timber.tag(TAG).v("Compressing ${toZip[i]} into $zipPath")

                val item = toZip[i]
                Timber.tag(TAG).v("Reading %s (size=%d)", item.path, item.path.length())
                item.path.inputStream().buffered().use { input ->
                    output.putNextEntry(ZipEntry(item.name))
                    input.copyTo(output)
                }
            }
        }

        Timber.tag(TAG).i("ZipFile finished: %s", zipPath)
    }

    data class Entry(
        val path: File,
        val name: String = path.name
    )

    companion object {
        private const val TAG = "ZipFile"
    }
}
