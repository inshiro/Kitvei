package na.kephas.kitvei.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import na.kephas.kitvei.fragment.FragmentBook
import na.kephas.kitvei.fragment.FragmentChapter
import na.kephas.kitvei.fragment.FragmentVerse

class SearchViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        return when(position) {
            0 -> FragmentBook()
            1 -> FragmentChapter()
            2 -> FragmentVerse()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {

        return when(position) {
            0 -> "BOOK"
            1 -> "CHAPTER"
            2 -> "VERSE"
            else -> "ERROR"
        }
    }
}