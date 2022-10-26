package de.rki.coronawarnapp.ui.dialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlin.experimental.ExperimentalTypeInference

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
class CwaDialogConfigBuilder {
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

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("titleInt")
    fun title(lambda: () -> Int) {
        title = IntOrString.IntRes(lambda())
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("titleString")
    fun title(lambda: () -> String) {
        title = IntOrString.StringRes(lambda())
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("messageInt")
    fun message(lambda: () -> Int) {
        message = IntOrString.IntRes(lambda())
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("messageString")
    fun message(lambda: () -> String) {
        message = IntOrString.StringRes(lambda())
    }

    fun positiveButton(lambda: ButtonBuilder.() -> Unit) {
        val contents = ButtonBuilder().apply(lambda).build()
        positiveButtonText = contents.text
        positiveButtonAction = contents.action
    }

    fun negativeButton(lambda: ButtonBuilder.() -> Unit) {
        val contents = ButtonBuilder().apply(lambda).build()
        negativeButtonText = contents.text
        negativeButtonAction = contents.action
    }

    fun neutralButton(lambda: ButtonBuilder.() -> Unit) {
        val contents = ButtonBuilder().apply(lambda).build()
        neutralButtonText = contents.text
        neutralButtonAction = contents.action
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

data class Button(
    val text: IntOrString,
    val action: DialogAction = { }
)

@CwaDialogDsl
class ButtonBuilder {
    private var text = IntOrString()
    private var action: DialogAction = { }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("positiveButtonInt")
    fun text(lambda: () -> Int) {
        text = IntOrString.IntRes(lambda())
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("positiveButtonString")
    fun text(lambda: () -> String) {
        text = IntOrString.StringRes(lambda())
    }

    fun action(lambda: () -> Unit) {
        action = { lambda() }
    }

    fun build() = Button(text, action)
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
