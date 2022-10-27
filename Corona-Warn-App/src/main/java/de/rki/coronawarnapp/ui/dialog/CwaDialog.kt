package de.rki.coronawarnapp.ui.dialog

import android.os.Parcelable
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

    fun title(resource: Int) {
        title = IntOrString.IntRes(resource)
    }

    fun title(text: String) {
        title = IntOrString.StringRes(text)
    }

    fun message(resource: Int) {
        message = IntOrString.IntRes(resource)
    }

    fun message(text: String) {
        message = IntOrString.StringRes(text)
    }

    fun positiveButton(resource: Int, lambda: () -> Unit = { }) {
        positiveButtonText = IntOrString.IntRes(resource)
        positiveButtonAction = lambda
    }

    fun positiveButton(text: String, lambda: () -> Unit = { }) {
        positiveButtonText = IntOrString.StringRes(text)
        positiveButtonAction = lambda
    }

    fun negativeButton(resource: Int, lambda: () -> Unit = { }) {
        negativeButtonText = IntOrString.IntRes(resource)
        negativeButtonAction = lambda
    }

    fun negativeButton(text: String, lambda: () -> Unit = { }) {
        negativeButtonText = IntOrString.StringRes(text)
        negativeButtonAction = lambda
    }

    fun neutralButton(resource: Int, lambda: () -> Unit = { }) {
        neutralButtonText = IntOrString.IntRes(resource)
        neutralButtonAction = lambda
    }

    fun neutralButton(text: String, lambda: () -> Unit = { }) {
        neutralButtonText = IntOrString.StringRes(text)
        neutralButtonAction = lambda
    }

    fun dismissAction(lambda: () -> Unit) {
        dismissAction = { lambda() }
    }

    fun isCancelable(lambda: () -> Boolean) {
        isCancelable = lambda()
    }

    fun isDeleteDialog(lambda: () -> Boolean) {
        isDeleteDialog = lambda()
    }

    fun customView(lambda: () -> Int) {
        customView = lambda()
    }

    fun build() = Triple(
        CwaDialogTexts(title, message, positiveButtonText, negativeButtonText, neutralButtonText),
        CwaDialogActions(positiveButtonAction, negativeButtonAction, neutralButtonAction, dismissAction),
        CwaDialogOptions(isCancelable, isDeleteDialog, customView)
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
    val customView: Int? = null
) : Parcelable

@Parcelize
open class IntOrString : Parcelable {
    @Parcelize
    data class StringRes(
        val stringResource: String
    ) : Parcelable, IntOrString()

    @Parcelize
    data class IntRes(
        val intResource: Int
    ) : Parcelable, IntOrString()
}
