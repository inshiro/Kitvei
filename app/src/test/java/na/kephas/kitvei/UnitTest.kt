package na.kephas.kitvei

import android.text.SpannableStringBuilder
import android.util.Log
import androidx.core.text.toSpannable
import androidx.lifecycle.ViewModelProviders
import na.kephas.kitvei.activity.MainActivity
import na.kephas.kitvei.data.AppDatabase
import na.kephas.kitvei.util.InjectorUtils
import na.kephas.kitvei.util.properSubstring
import na.kephas.kitvei.viewmodels.VerseListViewModel
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

    /*
        @Test
        @Throws(Exception::class)
        fun shouldNotBeNull() {
            assertNotNull(ma)
        }

        @Test
        fun listSize_is1189() {
            assertEquals(1189, vm.getPages().size)
        }*/

   // @Test
    @Ignore
    fun textTest() {
        val sText = SpannableStringBuilder("1  <<A Song of degrees.>> I will lift up mine eyes unto the hills, from whence cometh my help.")
        val exp = SpannableStringBuilder("I will lift up mine eyes unto the hills, from whence cometh my help.")


        sText.indexOf('<').let {
            if (it == 3)
                sText.properSubstring(sText.lastIndexOf('>') + 2)
            else if (sText.contains('<'))
                sText.properSubstring(0, it - 1)
        }

        println(sText)
        assertEquals(exp, sText)

    }

    @Test
    fun tTest(){

        val sText = SpannableStringBuilder("csdfewtxre4dfgsc4ter545tr")
        val s = sText.insert(0, "77")
        assertEquals("77csdfewtxre4dfgsc4ter545tr", sText.toString())
        println(s)
    }
    @After
    fun tearDown() {
        AppDatabase.destroyInstance()
        ma = null
    }

}
