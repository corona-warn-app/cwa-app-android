package de.rki.coronawarnapp.test.eventregistration.ui

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/** How to use
context.getSystemService<PrintManager>()?.apply {
print(
"CoronaWarnApp",
PrintingAdapter(it),
PrintAttributes.Builder().build()
)
}
 */
class PrintingAdapter(
    private val file: File
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder(file.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        callback.onLayoutFinished(info, oldAttributes != newAttributes)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) = try {

        FileInputStream(file).use { input ->
            FileOutputStream(destination.fileDescriptor).use { output ->
                input.copyTo(output)
            }
        }

        when {
            cancellationSignal.isCanceled -> callback.onWriteCancelled()
            else -> callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        }
    } catch (e: Exception) {
        callback.onWriteFailed(e.message)
        Timber.e(e, "Printing $file failed")
    }
}
