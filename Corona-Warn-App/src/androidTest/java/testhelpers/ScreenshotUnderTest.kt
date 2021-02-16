package testhelpers

/**
 * Similar to [Screenshot]. it is helpful during development and testing process to filter
 * the test currently being implemented.
 * In fastlane folder. replace `Screenshot` with `ScreenshotUnderTest` in Screengrabfile
 *
 * Note: this is only for testing purposes and should NOT be used in final tests
 */
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS
)
annotation class ScreenshotUnderTest
