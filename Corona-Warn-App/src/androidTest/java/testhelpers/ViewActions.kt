package testhelpers

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.google.android.material.tabs.TabLayout
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

fun recyclerScrollTo(position: Int? = null): ViewAction = RecyclerViewScrollAction(position)

private class RecyclerViewScrollAction(private val position: Int? = null) : ViewAction {
    override fun getDescription(): String {
        return "scroll RecyclerView to bottom"
    }

    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        val itemCount = recyclerView.adapter?.itemCount
        val itemPosition = position ?: itemCount?.minus(1) ?: 0
        recyclerView.scrollToPosition(itemPosition)
        uiController?.loopMainThreadUntilIdle()
    }
}

fun selectTabAtPosition(tabIndex: Int): ViewAction {
    return object : ViewAction {
        override fun getDescription() = "with tab at index $tabIndex"

        override fun getConstraints() = allOf(isDisplayed(), isAssignableFrom(TabLayout::class.java))

        override fun perform(uiController: UiController, view: View) {
            val tabLayout = view as TabLayout
            val tabAtIndex: TabLayout.Tab = tabLayout.getTabAt(tabIndex)
                ?: throw PerformException.Builder()
                    .withCause(Throwable("No tab at index $tabIndex"))
                    .build()

            tabAtIndex.select()
        }
    }
}
