@file:Suppress("PackageDirectoryMismatch")

package org.junit.jupiter.api

/**
 * Allows us to share common base test classes between unit and instrumentation tests.
 * Otherwise the compiler would complain about an unknown annotation class.
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class AfterAll
