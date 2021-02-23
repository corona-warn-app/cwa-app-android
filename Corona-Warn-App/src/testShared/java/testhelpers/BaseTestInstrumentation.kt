package testhelpers

import io.mockk.unmockkAll
import org.junit.AfterClass
import testhelpers.logging.JUnitTree
import timber.log.Timber

abstract class BaseTestInstrumentation {

    init {
        Timber.uprootAll()
        Timber.plant(JUnitTree())
        testClassName = this.javaClass.simpleName
    }

    companion object {
        private var testClassName: String? = null

        @JvmStatic
        @AfterClass
        fun onTestClassFinished() {
            unmockkAll()
            Timber.tag(testClassName).v("onTestClassFinished()")
            Timber.uprootAll()
        }
    }
}
