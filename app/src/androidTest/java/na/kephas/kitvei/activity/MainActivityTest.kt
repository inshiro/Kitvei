package na.kephas.kitvei.activity

import androidx.lifecycle.ViewModelProviders
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import na.kephas.kitvei.R
import na.kephas.kitvei.prefs
import na.kephas.kitvei.repository.MyViewModel
import na.kephas.kitvei.repository.AppDatabase
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private lateinit var vm: MyViewModel

    @Rule @JvmField
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        vm = ViewModelProviders.of(mActivityRule.activity).get(MyViewModel::class.java)
    }

    @Test
    fun swipeUntilEnd() {
        for (a in prefs.VP_Position..vm.getAll().size)
            onView(allOf(withId(R.id.mainViewPager), isCompletelyDisplayed())).perform(swipeLeft())
    }

    /*
        @Test
        fun useAppContext() {
            // Context of the app under test.
            val appContext = InstrumentationRegistry.getTargetContext()
            assertEquals("na.kephas.kitvei", appContext.packageName)
        }*/

    @After
    fun tearDown() {
        AppDatabase.destroyInstance()
    }
}