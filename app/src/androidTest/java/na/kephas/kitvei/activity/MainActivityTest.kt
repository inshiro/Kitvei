package na.kephas.kitvei.activity

import androidx.lifecycle.ViewModelProviders
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import na.kephas.kitvei.R
import na.kephas.kitvei.data.AppDatabase
import na.kephas.kitvei.Prefs
import na.kephas.kitvei.util.InjectorUtils
import na.kephas.kitvei.viewmodels.VerseListViewModel
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private lateinit var vm: VerseListViewModel

    @Rule @JvmField
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val factory = InjectorUtils.provideVerseListViewModelFactory(mActivityRule.activity)
        vm = ViewModelProviders.of(mActivityRule.activity, factory)
                .get(VerseListViewModel::class.java)
    }

    @Test
    fun swipeUntilEnd() {
        for (a in Prefs.VP_Position..1189)
            onView(allOf(withId(R.id.mainViewPager), isCompletelyDisplayed())).perform(swipeLeft())
    }

    @After
    fun tearDown() {
        AppDatabase.destroyInstance()
    }
}