package de.rki.coronawarnapp.ui.dialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlin.experimental.ExperimentalTypeInference

@DslMarker
annotation class CwaDialogDsl

typealias DialogAction = () -> Unit

data class CwaDialog(
    val texts: CwaDialogTexts,
    val actions: CwaDialogActions,
    val options: CwaDialogOptions
)

@CwaDialogDsl
class CwaDialogBuilder {
    private var config = CwaDialogTexts()
    private var actions = CwaDialogActions()
    private var options = CwaDialogOptions()

    fun configureTexts(lambda: CwaDialogTextsBuilder.() -> Unit) {
        config = CwaDialogTextsBuilder().apply(lambda).build()
    }

    fun configureActions(lambda: CwaDialogActionsBuilder.() -> Unit) {
        actions = CwaDialogActionsBuilder().apply(lambda).build()
    }

    fun configureOptions(lambda: CwaDialogOptionsBuilder.() -> Unit) {
        options = CwaDialogOptionsBuilder().apply(lambda).build()
    }

    fun build() = CwaDialog(config, actions, options)
}

@Parcelize
data class CwaDialogTexts(
    val title: @RawValue IntOrString? = null,
    val message: @RawValue IntOrString? = null,
    val positiveButton: @RawValue IntOrString? = null,
    val negativeButton: @RawValue IntOrString? = null,
    val neutralButton: @RawValue IntOrString? = null,
) : Parcelable

@CwaDialogDsl
class CwaDialogTextsBuilder {
    private var title = IntOrString()
    private var message = IntOrString()
    private var positiveButton = IntOrString()
    private var negativeButton = IntOrString()
    private var neutralButton = IntOrString()

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

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("positiveButtonInt")
    fun positiveButton(lambda: () -> Int) {
        positiveButton = IntOrString.IntRes(lambda())
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("positiveButtonString")
    fun positiveButton(lambda: () -> String) {
        positiveButton = IntOrString.StringRes(lambda())
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("negativeButtonInt")
    fun negativeButton(lambda: () -> Int) {
        negativeButton = IntOrString.IntRes(lambda())
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("negativeButtonString")
    fun negativeButton(lambda: () -> String) {
        negativeButton = IntOrString.StringRes(lambda())
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("neutralButtonInt")
    fun neutralButton(lambda: () -> Int) {
        neutralButton = IntOrString.IntRes(lambda())
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("neutralButtonString")
    fun neutralButton(lambda: () -> String) {
        neutralButton = IntOrString.StringRes(lambda())
    }

    fun build() = CwaDialogTexts(title, message, positiveButton, negativeButton, neutralButton)
}

data class CwaDialogActions(
    val positiveAction: DialogAction = { },
    val negativeAction: DialogAction = { },
    val neutralAction: DialogAction = { },
    val dismissAction: DialogAction = { }
)

class CwaDialogActionsBuilder {
    private var positiveAction: DialogAction = { }
    private var negativeAction: DialogAction = { }
    private var neutralAction: DialogAction = { }
    private var dismissAction: DialogAction = { }

    fun positiveAction(lambda: () -> Unit) {
        positiveAction = { lambda() }
    }

    fun negativeAction(lambda: () -> Unit) {
        negativeAction = { lambda() }
    }

    fun neutralAction(lambda: () -> Unit) {
        neutralAction = { lambda() }
    }

    fun dismissAction(lambda: () -> Unit) {
        dismissAction = { lambda() }
    }

    fun build() = CwaDialogActions(positiveAction, negativeAction, neutralAction, dismissAction)
}

@Parcelize
data class CwaDialogOptions(
    val isCancelable: Boolean = true,
    val isDeleteDialog: Boolean = false,
    val customView: Int? = null
) : Parcelable

@CwaDialogDsl
class CwaDialogOptionsBuilder {
    private var isCancelable: Boolean = true
    private var isDeleteDialog: Boolean = false
    private var customView: Int? = null

    fun isCancelable(lambda: () -> Boolean) {
        isCancelable = lambda()
    }

    fun isDeleteDialog(lambda: () -> Boolean) {
        isDeleteDialog = lambda()
    }

    fun customView(lambda: () -> Int) {
        customView = lambda()
    }

    fun build() = CwaDialogOptions(isCancelable, isDeleteDialog, customView)
}

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
