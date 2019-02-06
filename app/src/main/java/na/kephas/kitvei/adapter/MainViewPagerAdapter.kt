package na.kephas.kitvei.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.main_rv_item.view.*
import na.kephas.kitvei.R
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.page.Formatting
import na.kephas.kitvei.page.Page
import na.kephas.kitvei.theme.ThemeChooserDialog.Companion.fontSize
import na.kephas.kitvei.util.Fonts
import na.kephas.kitvei.util.TextControl
import na.kephas.kitvei.viewmodels.VerseListViewModel


class MainViewPagerAdapter(private val act: AppCompatActivity, private val vm: VerseListViewModel, private var currentList: List<Bible>) : PagerAdapter() /*, MainAdapter.ItemClickListener */ {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(act.applicationContext).inflate(R.layout.main_rv_item, collection, false) as ViewGroup
        collection.addView(layout)
        val row = currentList[position] //vm.getRow(position)//
        val textView = layout.MainTextView.apply {
            tag = "tv$position"
            typeface = Fonts.GentiumPlus_R
            setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize)
        }
        val scrollView = layout.MainScrollView.apply { tag = "sv$position" }
        var dropCapView: TextControl? = null
        if (Page.showDropCap) {
            val mLayout = FrameLayout(act)
            mLayout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dropCapView = TextControl(act).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tag = "dcv$position"
                typeface = Fonts.GentiumPlus_R
                setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize * 4.85f)
                setTextColor(Formatting.TextColorPrimary)
            }
            scrollView.removeAllViews()
            mLayout.addView(dropCapView, 0)
            mLayout.addView(textView, 1)
            scrollView.addView(mLayout)
            //setAutoSizeTextTypeWithDefaults(textView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
        }
        Page.display(vm, row, textView, dropCapView)

        return layout
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return currentList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === (`object` as View)
    }
}