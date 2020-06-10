/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.util

import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

        FileOutputStream(outputFile).use { fileOutputStream ->
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

    fun InputStream.unzip(callback: (entry: ZipEntry, entryContent: ByteArray) -> Any) =
        ZipInputStream(this).use {
            do {
                val entry = it.nextEntry
                if (entry != null) {
                    Timber.v("read zip entry ${entry.name}")
                    callback(entry, it.readBytes())
                    it.closeEntry()
                }
            } while (entry != null)
        }
}
