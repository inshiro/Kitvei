package na.kephas.kitvei.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AbsListView
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Field
import java.lang.reflect.Method
import android.view.ViewGroup




class VersesView : RecyclerView, AbsListView.OnScrollListener {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    val TAG = VersesView::class.java.simpleName

    private var mScrollState = 0

    // http://stackoverflow.com/questions/6369491/stop-listview-scroll-animation
    internal object StopListFling {

        private var mFlingEndField: Field? = null
        private var mFlingEndMethod: Method? = null

        init {
            try {
                mFlingEndField = AbsListView::class.java.getDeclaredField("mFlingRunnable")
                mFlingEndField!!.isAccessible = true
                mFlingEndMethod = mFlingEndField!!.type.getDeclaredMethod("endFling")
                mFlingEndMethod!!.isAccessible = true
            } catch (e: Exception) {
                mFlingEndMethod = null
            }

        }

        fun stop(list: RecyclerView) {
            if (mFlingEndMethod != null) {
                try {
                    mFlingEndMethod!!.invoke(mFlingEndField!!.get(list))
                } catch (ignored: Exception) {
                }

            }
        }
    }

    interface OnVerseScrollListener {
        fun onVerseScroll(v: VersesView, isPericope: Boolean, verse_1: Int, prop: Float)
        fun onScrollToTop(v: VersesView)
    }

    private val onVerseScrollListener: OnVerseScrollListener? = null

    private val userOnScrollListener: AbsListView.OnScrollListener? = null



    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        userOnScrollListener?.onScrollStateChanged(view, scrollState)

        this.mScrollState = scrollState
    }

    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        userOnScrollListener?.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount)

        if (onVerseScrollListener == null) return

        if (view?.childCount == 0) return

        var prop = 0f
        var position = -1

        val firstChild = view!!.getChildAt(0)
        val remaining = firstChild!!.bottom // padding top is ignored
        if (remaining >= 0) { // bottom of first child is lower than top padding
            position = firstVisibleItem
            prop = 1f - remaining.toFloat() / firstChild.height
        } else { // we should have a second child
            if (view.childCount > 1) {
                val secondChild = view.getChildAt(1)
                position = firstVisibleItem + 1
                prop = (-remaining).toFloat() / secondChild.height
            }
        }

        //val verse_1 = adapter.getVerseOrPericopeFromPosition(position)
        val verse_1 = adapter?.getItemId(position)?.toInt() ?: 0
        //val verse_1 = (adapter as? MainAdapter)?.getVerseFromPosition(position) ?: 0
        if (mScrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (verse_1 > 0) {
                onVerseScrollListener.onVerseScroll(this, false, verse_1, prop)
            } else {
                onVerseScrollListener.onVerseScroll(this, true, 0, 0f)
            }

            if (position == 0 && firstChild.top == this.paddingTop) {
                // we are really at the top
                onVerseScrollListener.onScrollToTop(this)
            }
        }
    }

    fun stopFling() {
        StopListFling.stop(this)
    }

    private val DRAG_EDGE_LEFT = 0x1

    val DRAG_EDGE_RIGHT = 0x1 shl 1
    val DRAG_EDGE_TOP = 0x1 shl 2
    val DRAG_EDGE_BOTTOM = 0x1 shl 3
    private val mDragEdge = DRAG_EDGE_LEFT
    private var mItemCount = 0
    private var mWidthMeasureSpec = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Sets up mListPadding
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        var childWidth = 0
        var childHeight = 0
        var childState = 0
        mItemCount = adapter?.itemCount?.let { it } ?: 0
        if (mItemCount > 0 && (widthMode == View.MeasureSpec.UNSPECIFIED || heightMode == View.MeasureSpec.UNSPECIFIED)) {
            val child = getChildAt(0)
            // Lay out child directly against the parent measure spec so that
            // we can obtain exected minimum width and height.

            measureScrapChild(child, 0, widthMeasureSpec, heightSize)
            childWidth = child.getMeasuredWidth()
            childHeight = child.getMeasuredHeight()
            childState = View.combineMeasuredStates(childState, child.getMeasuredState())
            /*if (recycleOnMeasure() && mRecycler.shouldRecycleViewType(
                            (child.getLayoutParams() as RecyclerView.LayoutParams).viewType)) {
                mRecycler.addScrapView(child, 0)

            }*/
        }
        if (widthMode == View.MeasureSpec.UNSPECIFIED) {
            widthSize = paddingLeft + paddingRight + childWidth +
                    verticalScrollbarWidth
        } else {
            widthSize = widthSize or (childState and View.MEASURED_STATE_MASK)
        }
        if (heightMode == View.MeasureSpec.UNSPECIFIED) {
            heightSize = paddingTop + paddingBottom + childHeight +
                    verticalFadingEdgeLength * 2
        }
        if (heightMode == View.MeasureSpec.AT_MOST) {
            // TODO: after first layout we should maybe start at the first visible position, not 0
            heightSize = measureHeightOfChildren(widthMeasureSpec, 0, RecyclerView.NO_POSITION, heightSize, -1)
        }
        setMeasuredDimension(widthSize, heightSize)
        mWidthMeasureSpec = widthMeasureSpec
    }

    private fun measureScrapChild(child: View, position: Int, widthMeasureSpec: Int, heightHint: Int) {
        var p = child.layoutParams
        if (p == null) {
            p = generateDefaultLayoutParams() as AbsListView.LayoutParams
            child.layoutParams = p
        }

        //p.viewType = adapter?.getItemViewType(position)
        //p.isEnabled = adapter.isEnabled(position)
        //p.forceAdd = true
        val childWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec,
                paddingLeft + paddingRight, p.width)
        val lpHeight = p.height
        val childHeightSpec: Int
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY)
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(heightHint, View.MeasureSpec.UNSPECIFIED)
        }
        child.measure(childWidthSpec, childHeightSpec)
        // Since this view was measured directly aginst the parent measure
        // spec, we must measure it again before reuse.
        child.forceLayout()
    }

    fun measureHeightOfChildren(widthMeasureSpec: Int, startPosition: Int, endPosition: Int,
                                maxHeight: Int, disallowPartialChildPosition: Int): Int {
        var endPosition = endPosition
        val adapter = adapter ?: return paddingTop + paddingBottom
// Include the padding of the list
        var returnedHeight = paddingTop + paddingBottom
        val dividerHeight = 0//divideerHegiht
        // The previous height value that was less than maxHeight and contained
        // no partial children
        var prevHeightWithoutPartialChild = 0
        var i: Int
        var child: View
        // mItemCount - 1 since endPosition parameter is inclusive
        endPosition = if (endPosition == RecyclerView.NO_POSITION) adapter.itemCount - 1 else endPosition
        //val recycleBin = mRecycler
       // val recyle = recycleOnMeasure()
        //val isScrap = mIsScrap
        i = startPosition
        while (i <= endPosition) {
            child = getChildAt(0)
            measureScrapChild(child, i, widthMeasureSpec, maxHeight)
            if (i > 0) {
                // Count the divider for all but one child
                returnedHeight += dividerHeight
            }
            // Recycle the view before we possibly return from the method
            /*if (recyle && recycleBin.shouldRecycleViewType(
                            (child.layoutParams as RecyclerView.LayoutParams).viewType)) {
                recycleBin.addScrapView(child, -1)
            }*/
            returnedHeight += child.measuredHeight
            if (returnedHeight >= maxHeight) {
                // We went over, figure out which height to return.  If returnedHeight > maxHeight,
                // then the i'th position did not fit completely.
                return if (disallowPartialChildPosition >= 0 // Disallowing is enabled (> -1)

                        && i > disallowPartialChildPosition // We've past the min pos

                        && prevHeightWithoutPartialChild > 0 // We have a prev height

                        && returnedHeight != maxHeight // i'th child did not fit completely
                )
                    prevHeightWithoutPartialChild
                else
                    maxHeight
            }
            if (disallowPartialChildPosition >= 0 && i >= disallowPartialChildPosition) {
                prevHeightWithoutPartialChild = returnedHeight
            }
            ++i
        }
        // At this point, we went through the range of children, and they each
        // completely fit, so return the returnedHeight
        return returnedHeight
    }
  /*
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        /*this.post {
            kotlinx.coroutines.experimental.launch(na.kephas.kitvei.util.backgroundPool) {

            while ( childCount < 2) {
                d { "Delaying for layout children... " }
                    kotlinx.coroutines.experimental.delay(1000)
            }
            if (childCount < 2) {
                throw RuntimeException("Layout must have two children")
            }
            }

        }*/

        val params = layoutParams

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        var desiredWidth = 0
        var desiredHeight = 0

        var wrapChildHeight = 0
        var wrapChildWidth = 0

        d{"$childCount"}
        // first find the largest child
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childParams = child.layoutParams
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            if (childParams.height == RecyclerView.LayoutParams.WRAP_CONTENT && (mDragEdge === DRAG_EDGE_LEFT || mDragEdge === DRAG_EDGE_RIGHT))
                wrapChildHeight = Math.max(child.measuredHeight, wrapChildHeight)
            else
                desiredHeight = Math.max(child.measuredHeight, desiredHeight)

            if (childParams.width == RecyclerView.LayoutParams.WRAP_CONTENT && (mDragEdge === DRAG_EDGE_TOP || mDragEdge === DRAG_EDGE_BOTTOM))
                wrapChildWidth = Math.max(child.measuredWidth, wrapChildWidth)
            else
                desiredWidth = Math.max(child.measuredWidth, desiredWidth)
        }
        if (wrapChildHeight != 0) desiredHeight = wrapChildHeight
        if (wrapChildWidth != 0) desiredWidth = wrapChildWidth

        // create new measure spec using the largest child width
        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(desiredWidth, widthMode)
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(desiredHeight, View.MeasureSpec.EXACTLY)

        val measuredHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val measuredWidth = View.MeasureSpec.getSize(widthMeasureSpec)

        /*
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childParams = child.layoutParams

            if (childParams != null) {
                if (childParams.height != RecyclerView.LayoutParams.WRAP_CONTENT) {
                    childParams.height = measuredHeight
                }

                if (childParams.width == RecyclerView.LayoutParams.MATCH_PARENT) {
                    childParams.width = measuredWidth
                }
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }*/

        // taking accounts of padding
        desiredWidth += paddingLeft + paddingRight
        desiredHeight += paddingTop + paddingBottom

        // adjust desired width
        if (widthMode == View.MeasureSpec.EXACTLY) {
            desiredWidth = measuredWidth
        } else {
            if (params.width == RecyclerView.LayoutParams.MATCH_PARENT) {
                desiredWidth = measuredWidth
            }

            if (widthMode == View.MeasureSpec.AT_MOST) {
                desiredWidth = if (desiredWidth > measuredWidth) measuredWidth else desiredWidth
            }
        }

        // adjust desired height
        if (heightMode == View.MeasureSpec.EXACTLY) {
            desiredHeight = measuredHeight
        } else {
            if (params.height == RecyclerView.LayoutParams.MATCH_PARENT) {
                desiredHeight = measuredHeight
            }

            if (heightMode == View.MeasureSpec.AT_MOST) {
                desiredHeight = if (desiredHeight > measuredHeight) measuredHeight else desiredHeight
            }
        }

        setMeasuredDimension(desiredWidth, desiredHeight)
    }
*/
}