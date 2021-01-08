package testhelpers

import testhelpers.logging.JUnitTree
import timber.log.Timber

abstract class BaseTest {

    init {
        Timber.uprootAll()
        Timber.plant(JUnitTree())
    }
}
