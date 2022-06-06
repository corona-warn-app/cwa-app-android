package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintDocumentAdapter.LayoutResultCallback
import android.print.PrintDocumentAdapter.WriteResultCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PdfFile(private val printAttributes: PrintAttributes) {
    suspend fun save(printAdapter: PrintDocumentAdapter, path: File, fileName: String) =
        suspendCancellableCoroutine<Boolean> { cont ->
            printAdapter.onLayout(null, printAttributes, null, object : LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
                    printAdapter.onWrite(
                        arrayOf(PageRange.ALL_PAGES),
                        getOutputFile(path,fileName),
                        CancellationSignal(),
                        object : WriteResultCallback() {
                            override fun onWriteFinished(pages: Array<PageRange>) {
                                super.onWriteFinished(pages)
                                cont.resume(true)
                            }

                            override fun onWriteFailed(error: CharSequence?) {
                                super.onWriteFailed(error)
                                cont.resumeWithException(Exception(error.toString()))
                            }

                            override fun onWriteCancelled() {
                                super.onWriteCancelled()
                                cont.cancel()
                            }
                        })
                }
            }, null)
        }

    private fun getOutputFile(path: File, filenam: String): ParcelFileDescriptor? {
        try {

            if (!path.exists()) {
                path.mkdir()
            }
            val file = File(path, filenam)
            file.createNewFile()
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to open ParcelFileDescriptor", e)
        }
        return null
    }

    companion object {
        private val TAG = PdfFile::class.java.simpleName
    }
}
