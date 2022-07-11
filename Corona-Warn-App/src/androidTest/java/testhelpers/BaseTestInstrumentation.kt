package testhelpers

import io.mockk.unmockkAll
import org.junit.AfterClass
import timber.log.Timber

abstract class BaseTestInstrumentation {

    init {
        Timber.uprootAll()
        Timber.plant(JUnitTree())
        testClassName = this::class.simpleName.toString()
    }

    companion object {
        private var testClassName: String = ""

        @JvmStatic
        @AfterClass
        fun onTestClassFinished() {
            unmockkAll()
            Timber.tag(testClassName).v("onTestClassFinished()")
            Timber.uprootAll()
        }
    }
}
