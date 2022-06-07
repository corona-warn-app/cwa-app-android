package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintDocumentAdapter.LayoutResultCallback
import android.print.PrintDocumentAdapter.WriteResultCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FilePrinter(private val attributes: PrintAttributes) {
    suspend fun print(printAdapter: PrintDocumentAdapter, path: File, fileName: String) =
        suspendCancellableCoroutine<Unit> { cont ->
            printAdapter.onLayout(
                null, attributes,
                null,
                object : LayoutResultCallback() {
                    override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
                        printAdapter.onWrite(
                            arrayOf(PageRange.ALL_PAGES),
                            getOutputFileDescriptor(path, fileName),
                            CancellationSignal(),
                            object : WriteResultCallback() {
                                override fun onWriteFinished(pages: Array<PageRange>) = cont.resume(Unit)

                                override fun onWriteFailed(error: CharSequence?) =
                                    cont.resumeWithException(Exception(error.toString()))
                            }
                        )
                    }
                },
                null
            )
        }

    private fun getOutputFileDescriptor(path: File, fileName: String): ParcelFileDescriptor {
        if (!path.exists()) path.mkdir()
        val file = File(path, fileName).also { it.createNewFile() }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)
    }
}
