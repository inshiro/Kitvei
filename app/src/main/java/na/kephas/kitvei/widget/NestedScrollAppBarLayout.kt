package na.kephas.kitvei.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator

import com.google.android.material.appbar.AppBarLayout

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat

/**
 * Nested scroll app bar layout.
 *
 * An AppBarLayout that can dispatch nested scrolling action.
 *
 */

@CoordinatorLayout.DefaultBehavior(NestedScrollAppBarLayout.Behavior::class)
class NestedScrollAppBarLayout : AppBarLayout, NestedScrollingChild {

    private var nestedScrollingChildHelper: NestedScrollingChildHelper? = null
    internal var nestedScrollingListener: OnNestedScrollingListener? = null

    private var animator: AutomaticScrollAnimator? = null

    var touchSlop: Float = 0.toFloat()
        private set

    var startY: Float = 0.toFloat()

    // an appbar has 3 part : scroll / enterAlways / without scroll flag.
    private var scrollHeight: Int = 0
    private var enterAlwaysHeight: Int = 0
    private var staticHeight: Int = 0

    class Behavior : AppBarLayout.Behavior {

        private var appBarLayout: NestedScrollAppBarLayout? = null

        private var oldY: Float = 0.toFloat()
        private var isBeingDragged: Boolean = false

        constructor() : super() {}

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

        private fun bindAppBar(child: AppBarLayout) {
            if (appBarLayout == null) {
                this.appBarLayout = child as NestedScrollAppBarLayout
            }
        }

        override fun onTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent): Boolean {
            bindAppBar(child)
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    appBarLayout!!.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                    oldY = ev.y
                    isBeingDragged = false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isBeingDragged) {
                        if (Math.abs(ev.y - oldY) > appBarLayout!!.touchSlop) {
                            isBeingDragged = true
                        }
                    }
                    if (isBeingDragged) {
                        val total = intArrayOf(0, (oldY - ev.y).toInt())
                        val consumed = intArrayOf(0, 0)
                        appBarLayout!!.dispatchNestedPreScroll(
                                total[0], total[1], consumed, null)
                        appBarLayout!!.dispatchNestedScroll(
                                consumed[0], consumed[1], total[0] - consumed[0], total[1] - consumed[1], null)
                    }
                    oldY = ev.y
                    return isBeingDragged
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    appBarLayout!!.stopNestedScroll()
                    if (isBeingDragged) {
                        isBeingDragged = false
                        return true
                    }
                }
            }

            return super.onTouchEvent(parent, child, ev)
        }

        override fun onStartNestedScroll(parent: CoordinatorLayout, child: AppBarLayout,
                                         directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
            if (super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type) && type == 0) {
                bindAppBar(child)
                if (appBarLayout!!.nestedScrollingListener != null) {
                    appBarLayout!!.nestedScrollingListener!!.onStartNestedScroll()
                }
                appBarLayout!!.stopScrollAnimator()
                appBarLayout!!.startY = child.y
                return true
            } else {
                return false
            }
        }

        override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout,
                                       target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
            bindAppBar(child)
            if (appBarLayout!!.nestedScrollingListener != null) {
                appBarLayout!!.nestedScrollingListener!!.onNestedScrolling()
            }
        }

        override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout,
                                    target: View, dxConsumed: Int, dyConsumed: Int,
                                    dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
            super.onNestedScroll(
                    coordinatorLayout, child, target,
                    dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
            bindAppBar(child)
            if (appBarLayout!!.nestedScrollingListener != null) {
                appBarLayout!!.nestedScrollingListener!!.onNestedScrolling()
            }
        }

        override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout,
                                        target: View, type: Int) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type)
            bindAppBar(child)
            if (appBarLayout!!.nestedScrollingListener != null) {
                appBarLayout!!.nestedScrollingListener!!.onStopNestedScroll()
            }

            val top = child.y
            val height = child.measuredHeight.toFloat()
            val bottom = top + height
            appBarLayout!!.computerHeightData()

            if (appBarLayout!!.scrollHeight > 0 || appBarLayout!!.enterAlwaysHeight > 0) {
                if (appBarLayout!!.startY == top) {
                    return
                }
                if (appBarLayout!!.startY > top) {  // drag up.
                    appBarLayout!!.hideTopBar(this)
                } else if (appBarLayout!!.startY < top) { // drag down.
                    if (bottom > appBarLayout!!.enterAlwaysHeight + appBarLayout!!.staticHeight) {
                        appBarLayout!!.showTopBar(this)
                    } else if (bottom > appBarLayout!!.staticHeight) {
                        appBarLayout!!.showEnterAlwaysBar(this)
                    }
                }
            }
        }
    }

    private inner class AutomaticScrollAnimator internal constructor(behavior: AppBarLayout.Behavior?, toY: Int) : ValueAnimator() {

        private var lastY: Int = 0

        init {
            val fromY = y.toInt()
            this.lastY = fromY

            setIntValues(fromY, toY)
            duration = (150.0 + 150.0 * Math.abs(toY - fromY) / measuredHeight).toLong()
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                if (behavior != null) {
                    val newY = animation.animatedValue as Int
                    val total = intArrayOf(0, lastY - newY)
                    val consumed = intArrayOf(0, 0)
                    behavior.onNestedPreScroll(
                            parent as CoordinatorLayout, this@NestedScrollAppBarLayout, this@NestedScrollAppBarLayout,
                            total[0], total[1], consumed, 0)
                    behavior.onNestedScroll(
                            parent as CoordinatorLayout, this@NestedScrollAppBarLayout, this@NestedScrollAppBarLayout,
                            consumed[0], consumed[1], total[0] - consumed[0], total[1] - consumed[1], 0)
                    lastY = newY
                }
            }
        }
    }

    constructor(context: Context) : super(context) {
        this.initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.initialize()
    }

    private fun initialize() {
        this.nestedScrollingChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true

        this.touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    }

    /**
     * Do animation to expand the whole AppBarLayout.
     */
    fun showTopBar(behavior: AppBarLayout.Behavior) {
        stopScrollAnimator()
        doScrollAnimation(behavior, 0)
    }

    /**
     * Do animation to expand the part of AppBarLayout which has "enterAlways" flag.
     */
    fun showEnterAlwaysBar(behavior: AppBarLayout.Behavior) {
        stopScrollAnimator()
        doScrollAnimation(behavior, -scrollHeight)
    }

    /**
     * Do animation to hide the part of AppBarLayout which has "scroll" flag.
     */
    fun hideTopBar(behavior: AppBarLayout.Behavior) {
        stopScrollAnimator()
        doScrollAnimation(behavior, staticHeight - measuredHeight)
    }

    private fun doScrollAnimation(behavior: AppBarLayout.Behavior, toY: Int) {
        if (y != toY.toFloat()) {
            this.animator = AutomaticScrollAnimator(behavior, toY)
            animator!!.start()
        }
    }

    fun stopScrollAnimator() {
        if (animator != null) {
            animator!!.cancel()
        }
    }

    /**
     * compute the height of three part in AppBarLayout.
     */
    internal fun computerHeightData() {
        staticHeight = 0
        enterAlwaysHeight = staticHeight
        scrollHeight = enterAlwaysHeight
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            val params = v.layoutParams as AppBarLayout.LayoutParams
            val flags = params.scrollFlags
            if (flags and AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP == AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP) {
                staticHeight = 0
                enterAlwaysHeight = staticHeight
                scrollHeight = enterAlwaysHeight
                return
            } else if (flags and AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL != AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL) {
                staticHeight += v.measuredHeight
            } else if (flags and AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS == AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS) {
                enterAlwaysHeight += v.measuredHeight
            } else {
                scrollHeight += v.measuredHeight
            }
        }
    }

    // interface.

    // on nested scrolling listener.

    interface OnNestedScrollingListener {
        fun onStartNestedScroll()
        fun onNestedScrolling()
        fun onStopNestedScroll()
    }

    fun setOnNestedScrollingListener(l: OnNestedScrollingListener) {
        this.nestedScrollingListener = l
    }

    // nested scrolling child.

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        nestedScrollingChildHelper!!.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return nestedScrollingChildHelper!!.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return nestedScrollingChildHelper!!.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        nestedScrollingChildHelper!!.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return nestedScrollingChildHelper!!.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int,
                                      dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return nestedScrollingChildHelper!!.dispatchNestedScroll(
                dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return nestedScrollingChildHelper!!.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return nestedScrollingChildHelper!!.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return nestedScrollingChildHelper!!.dispatchNestedPreFling(velocityX, velocityY)
    }
}