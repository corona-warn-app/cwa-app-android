package testhelpers

/**
 * Annotation for testing screenshots purpose.
 * To give the developer a warning that the annotated API should be used in final tests.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This function is only for testing. It should not be used in final tests"
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class TestFirebaseScreenshot
