package testhelpers

/**
 * Annotation for Screenshots test cases. it help to mark those cases and run them alone
 */
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS
)
annotation class Screenshot
