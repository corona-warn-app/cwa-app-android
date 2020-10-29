package de.rki.coronawarnapp.util.worker

import io.github.classgraph.ClassGraph
import io.kotest.matchers.collections.shouldContainAll
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber

class WorkerBinderTest : BaseTest() {

    /**
     * If one of our factories is not part of the factory map provided to **[CWAWorkerFactory]**,
     * then the lookup will fail and an exception thrown.
     * This can't be checked at compile-time and may create subtle errors that will not immediately be caught.
     *
     * This test uses the ClassGraph library to scan our package, find all worker classes,
     * and makes sure that they are all bound into our factory map.
     * Creating a new factory that is not registered or removing one from **[WorkerBinder]**
     * will cause this test to fail.
     */
    @Test
    fun `all worker factory are bound into the factory map`() {
        val component = DaggerWorkerTestComponent.factory().create()
        val factories = component.factories

        Timber.v("We know %d worker factories.", factories.size)
        factories.keys.forEach {
            Timber.v("Registered: ${it.name}")
        }
        require(component.factories.isNotEmpty())

        val scanResult = ClassGraph()
            .acceptPackages("de.rki.coronawarnapp")
            .enableClassInfo()
            .scan()

        val ourWorkerClasses = scanResult
            .getSubclasses("androidx.work.ListenableWorker")
            .filterNot { it.name.startsWith("androidx.work") }

        Timber.v("Our project contains %d worker classes.", ourWorkerClasses.size)
        ourWorkerClasses.forEach { Timber.v("Existing: ${it.name}") }

        val boundFactories = factories.keys.map { it.name }
        val existingFactories = ourWorkerClasses.map { it.name }
        boundFactories shouldContainAll existingFactories
    }
}
