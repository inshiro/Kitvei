package na.kephas.kitvei

import androidx.lifecycle.ViewModelProviders
import na.kephas.kitvei.activity.MainActivity
import na.kephas.kitvei.data.AppDatabase
import na.kephas.kitvei.util.InjectorUtils
import na.kephas.kitvei.viewmodels.VerseListViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(21), constants = BuildConfig::class)
@FixMethodOrder(MethodSorters.JVM)
class UnitTest {

    private lateinit var vm: VerseListViewModel
    private var ma: MainActivity? = null

    @Before
    fun setup() {
        ma = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val factory = InjectorUtils.provideVerseListViewModelFactory(ma!!)
        vm = ViewModelProviders.of(ma!!, factory)
                .get(VerseListViewModel::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotBeNull() {
        assertNotNull(ma)
    }

    @Test
    fun listSize_is1189() {
        assertEquals(1189, vm.getPages().size)
    }

    @After
    fun tearDown() {
        AppDatabase.destroyInstance()
        ma = null
    }

}
