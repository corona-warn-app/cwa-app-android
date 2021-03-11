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

/**
 * Printing adapter for poster PDF files
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
            Timber.i("onLayoutCancelled")
            return
        }

        val info = PrintDocumentInfo.Builder(file.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        Timber.i(
            "onLayoutFinished(info:%s, oldAttributes:%s, newAttributes:%s)",
            info,
            oldAttributes,
            newAttributes
        )
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
                val bytesCopied = input.copyTo(output)
                Timber.i("bytesCopied:$bytesCopied")
            }
        }

        when {
            cancellationSignal.isCanceled -> {
                Timber.i("onWriteCancelled")
                callback.onWriteCancelled()
            }
            else -> {
                Timber.i("onWriteFinished")
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        }
    } catch (e: Exception) {
        callback.onWriteFailed(e.message)
        Timber.e(e, "Printing $file failed")
    }
}
