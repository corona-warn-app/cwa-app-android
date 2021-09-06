package testhelpers

import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import testhelpers.logging.JUnitTree
import timber.log.Timber

@Suppress("UnnecessaryAbstractClass", "UtilityClassWithPublicConstructor")
abstract class BaseTest {

    init {
        Timber.uprootAll()
        Timber.plant(JUnitTree())
        testClassName = this::class.simpleName.toString()
    }

    companion object {
        private var testClassName: String = ""

        @JvmStatic
        @AfterAll
        fun onTestClassFinished() {
            unmockkAll()
            Timber.tag(testClassName).v("onTestClassFinished()")
            Timber.uprootAll()
        }
    }
}
