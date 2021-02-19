package testhelpers

import io.mockk.unmockkAll
import org.junit.AfterClass
import org.junit.jupiter.api.AfterAll
import testhelpers.logging.JUnitTree
import timber.log.Timber

abstract class BaseTest {

    init {
        Timber.uprootAll()
        Timber.plant(JUnitTree())
        testClassName = this.javaClass.simpleName
    }

    companion object {
        private var testClassName: String? = null

        @JvmStatic
        @AfterClass
        fun onTestClassFinishedJUnit4() {
            onTestClassFinished()
        }

        @JvmStatic
        @AfterAll
        fun onTestClassFinishedJUnit5() {
            onTestClassFinished()
        }

        private fun onTestClassFinished() {
            unmockkAll()
            Timber.tag(testClassName).v("onTestClassFinished()")
            Timber.uprootAll()
        }
    }
}
