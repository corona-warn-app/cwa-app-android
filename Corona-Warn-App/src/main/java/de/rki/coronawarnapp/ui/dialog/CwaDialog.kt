package de.rki.coronawarnapp.ui.dialog

import android.os.Parcelable
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@DslMarker
annotation class CwaDialogDsl

typealias DialogAction = () -> Unit

@Parcelize
data class CwaDialogTexts(
    val title: @RawValue IntOrString? = null,
    val message: @RawValue IntOrString? = null,
    val positiveButtonText: @RawValue IntOrString? = null,
    val negativeButtonText: @RawValue IntOrString? = null,
    val neutralButtonText: @RawValue IntOrString? = null,
) : Parcelable

@CwaDialogDsl
class CwaDialogBuilder {
    private var title = IntOrString()
    private var message = IntOrString()
    private var positiveButtonText = IntOrString()
    private var positiveButtonAction: DialogAction = { }
    private var negativeButtonText = IntOrString()
    private var negativeButtonAction: DialogAction = { }
    private var neutralButtonText = IntOrString()
    private var neutralButtonAction: DialogAction = { }
    private var dismissAction: DialogAction = { }
    private var isCancelable: Boolean = true
    private var isDeleteDialog: Boolean = false
    private var customView: Int? = null
    private var throwableError: Throwable? = null

    fun title(@StringRes resource: Int) {
        title = IntOrString.IntResource(resource)
    }

    fun title(text: String) {
        title = IntOrString.StringResource(text)
    }

    fun message(@StringRes resource: Int) {
        message = IntOrString.IntResource(resource)
    }

    fun message(text: String) {
        message = IntOrString.StringResource(text)
    }

    fun positiveButton(@StringRes resource: Int, lambda: () -> Unit = { }) {
        positiveButtonText = IntOrString.IntResource(resource)
        positiveButtonAction = lambda
    }

    fun positiveButton(text: String, lambda: () -> Unit = { }) {
        positiveButtonText = IntOrString.StringResource(text)
        positiveButtonAction = lambda
    }

    fun negativeButton(@StringRes resource: Int, lambda: () -> Unit = { }) {
        negativeButtonText = IntOrString.IntResource(resource)
        negativeButtonAction = lambda
    }

    fun negativeButton(text: String, lambda: () -> Unit = { }) {
        negativeButtonText = IntOrString.StringResource(text)
        negativeButtonAction = lambda
    }

    fun neutralButton(@StringRes resource: Int, lambda: () -> Unit = { }) {
        neutralButtonText = IntOrString.IntResource(resource)
        neutralButtonAction = lambda
    }

    fun neutralButton(text: String, lambda: () -> Unit = { }) {
        neutralButtonText = IntOrString.StringResource(text)
        neutralButtonAction = lambda
    }

    fun dismissAction(lambda: () -> Unit) {
        dismissAction = { lambda() }
    }

    fun setCancelable(cancelable: Boolean) {
        isCancelable = cancelable
    }

    fun setDeleteDialog(isDelete: Boolean) {
        isDeleteDialog = isDelete
    }

    fun setView(@LayoutRes view: Int) {
        customView = view
    }

    fun setError(error: Throwable?) {
        throwableError = error
    }

    fun build() = Triple(
        CwaDialogTexts(title, message, positiveButtonText, negativeButtonText, neutralButtonText),
        CwaDialogActions(positiveButtonAction, negativeButtonAction, neutralButtonAction, dismissAction),
        CwaDialogOptions(isCancelable, isDeleteDialog, customView, throwableError)
    )
}

data class CwaDialogActions(
    val positiveAction: DialogAction = { },
    val negativeAction: DialogAction = { },
    val neutralAction: DialogAction = { },
    val dismissAction: DialogAction = { }
)

@Parcelize
data class CwaDialogOptions(
    val isCancelable: Boolean = true,
    val isDeleteDialog: Boolean = false,
    val customView: Int? = null,
    val throwableError: Throwable? = null
) : Parcelable

@Parcelize
open class IntOrString : Parcelable {
    @Parcelize
    data class StringResource(
        val stringResource: String
    ) : Parcelable, IntOrString()

    @Parcelize
    data class IntResource(
        @StringRes val intResource: Int
    ) : Parcelable, IntOrString()
}
