package de.rki.coronawarnapp.util

import KeyExportFormat
import KeyExportFormat.TEKSignatureList
import KeyExportFormat.TEKSignatureList.newBuilder
import KeyExportFormat.TemporaryExposureKeyExport
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import de.rki.coronawarnapp.util.ProtoFormatConverterExtensions.convertToGoogleKey
import de.rki.coronawarnapp.util.TimeAndDateExtensions.logUTCFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.util.Date
import java.util.UUID

object KeyFileHelper {
    // DO NOT REMOVE WHITESPACES, PADDED 16 BYTE STRING HEADER
    private const val EXPORT_FILE_HEADER = "EK Export v1    "
    private const val EXPORT_FILE_HEADER_CHARSET = "UTF8"

    private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
    private const val EXPORT_BINARY_FILE_NAME = "export.bin"

    private const val BYTES_IN_KILOBYTES = 1024

    private val TAG: String? = KeyFileHelper::class.simpleName

    suspend fun asyncCreateExportFiles(
        fileList: List<AppleLegacyKeyExchange.File>,
        storageDirectory: File?
    ): List<File> = withContext(Dispatchers.IO) {
        fileList.map { file ->
            async(Dispatchers.IO) {
                val createdExportString = "created export for batch ${file.header.batchNum}" +
                        "(of ${file.header.batchSize}) with ${file.keysCount} keys, BEG:${Date(
                            file.header.startTimestamp
                        ).logUTCFormat()}, END:${Date(file.header.endTimestamp).logUTCFormat()}"
                Timber.d(createdExportString)
                Pair(
                    TemporaryExposureKeyExport
                        .newBuilder()
                        .addAllKeys(file.keysList.map { appleKey -> appleKey.convertToGoogleKey() })
                        .addSignatureInfos(SignatureHelper.clientSig)
                        .setBatchNum(file.header.batchNum)
                        .setBatchSize(file.header.batchSize)
                        .setRegion(file.header.region)
                        .setStartTimestamp(file.header.startTimestamp)
                        .setEndTimestamp(file.header.endTimestamp)
                        .build(),
                    KeyExportFormat.TEKSignature.newBuilder()
                        .setBatchNum(file.header.batchNum)
                        .setBatchSize(file.header.batchSize)
                        .setSignatureInfo(SignatureHelper.clientSig)
                        .build()
                )
            }
        }.awaitAll()
            .mapIndexed { _, source ->
                async(Dispatchers.IO) {
                    createBinaryFile(
                        storageDirectory,
                        "${source.first.batchNum}-" +
                                "${source.first.startTimestamp}-" +
                                "${source.first.endTimestamp}.zip",
                        source
                    )
                }
            }.awaitAll()
    }

    private suspend fun createBinaryFile(
        storageDirectory: File?,
        zipFileName: String,
        sourceWithTEKSignature: Pair<TemporaryExposureKeyExport, KeyExportFormat.TEKSignature>
    ): File {
        return withContext(Dispatchers.IO) {
            val exportFile = async {
                generateBinaryFile(
                    storageDirectory,
                    sourceWithTEKSignature.first
                )
            }

            val exportSignatureFile = async {
                generateSignatureFile(
                    storageDirectory,
                    newBuilder().addAllSignatures(listOf(sourceWithTEKSignature.second)).build()
                )
            }

            return@withContext ZipHelper.createZipFile(
                storageDirectory,
                zipFileName,
                listOf(
                    Pair(EXPORT_BINARY_FILE_NAME, exportFile.await()),
                    Pair(EXPORT_SIGNATURE_FILE_NAME, exportSignatureFile.await())
                )
            ).also {
                Timber.d("output file name:${it.absolutePath}")
                Timber.d("output file size:${it.length() / BYTES_IN_KILOBYTES} KB")

                exportFile.await().delete()
                exportSignatureFile.await().delete()
            }
        }
    }

    private fun generateSignatureFile(
        storageDirectory: File?,
        signatures: TEKSignatureList
    ): File {
        val exportSignatureFile =
            File(storageDirectory, "key-export-signature-${UUID.randomUUID()}.sig")
        if (exportSignatureFile.exists()) exportSignatureFile.delete()
        exportSignatureFile.createNewFile()
        signatures.writeToFile(exportSignatureFile)
        return exportSignatureFile
    }

    private fun TEKSignatureList.writeToFile(
        file: File
    ) = FileOutputStream(file).use { stream ->
        this.writeTo(stream)
    }

    private fun generateBinaryFile(
        storageDirectory: File?,
        source: TemporaryExposureKeyExport
    ): File {
        val exportBinaryFile = File(storageDirectory, getExportBinaryFileName())
        if (exportBinaryFile.exists()) exportBinaryFile.delete()
        exportBinaryFile.createNewFile()
        exportBinaryFile.appendBinaryHeader()
        exportBinaryFile.appendBytes(source.toByteArray())
        return exportBinaryFile
    }

    private fun getExportBinaryFileName(): String = "key-export-binary-${UUID.randomUUID()}.bin"

    private fun File.appendBinaryHeader() = FileOutputStream(this).use { fos ->
        OutputStreamWriter(fos, Charset.forName(EXPORT_FILE_HEADER_CHARSET)).use { osw ->
            BufferedWriter(osw).use { bw ->
                bw.write(EXPORT_FILE_HEADER)
            }
        }
    }
}
