package na.kephas.kitvei

import android.util.Log
import androidx.lifecycle.ViewModelProviders
import na.kephas.kitvei.activity.MainActivity
import na.kephas.kitvei.repository.AppDatabase
import na.kephas.kitvei.repository.MyViewModel
import org.junit.*
import org.junit.Assert.*
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

    private lateinit var vm: MyViewModel
    private var ma: MainActivity? = null


    @Before
    fun setup() {
        ma = Robolectric.buildActivity(MainActivity::class.java).create().get()
        vm = ViewModelProviders.of(ma!!).get(MyViewModel::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotBeNull() {
        assertNotNull(ma)
    }

    @Test
    fun listSize_is1189() {
        assertEquals(1189, vm.getAll().size)
    }

    /*
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }*/

    @After
    fun tearDown() {
        AppDatabase.destroyInstance()
        ma = null
    }

}
