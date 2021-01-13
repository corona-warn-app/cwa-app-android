package de.rki.coronawarnapp.util

import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipHelper {
    private val TAG: String? = ZipHelper::class.simpleName

    fun createZipFile(
        directory: File?,
        nameOfOutputFile: String,
        filesWithNames: Iterable<Pair<String, File>>
    ): File {
        val outputFile = File(directory, nameOfOutputFile)

        if (outputFile.exists()) {
            Timber.d("file with output name already exists, override.")
            outputFile.delete()
        }
        outputFile.createNewFile()

        outputFile.outputStream().use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { zipOutputStream ->
                writeToZip(
                    nameOfOutputFile,
                    directory,
                    zipOutputStream,
                    filesWithNames
                )
            }
        }

        return outputFile
    }

    private fun writeToZip(
        nameOfOutputFile: String,
        directory: File?,
        zipOutputStream: ZipOutputStream,
        filesWithName: Iterable<Pair<String, File>>
    ) = filesWithName.forEach { file ->
        Timber.d("writing ${file.second.name} as ${file.first} to $nameOfOutputFile in $directory")
        val fileInputStream = FileInputStream(file.second)
        val zipEntry = ZipEntry(file.first)
        zipOutputStream.putNextEntry(zipEntry)

        zipOutputStream.write(fileInputStream.readBytes())

        zipOutputStream.closeEntry()
    }

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
