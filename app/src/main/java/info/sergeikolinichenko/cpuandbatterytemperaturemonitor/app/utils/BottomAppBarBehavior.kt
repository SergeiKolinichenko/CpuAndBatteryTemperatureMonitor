package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.bottomappbar.BottomAppBar
import kotlin.properties.Delegates

/** Created by Sergei Kolinichenko on 10.11.2022 at 14:01 (GMT+3) **/

class BottomAppBarBehavior(
    context: Context, attributeSet: AttributeSet
): CoordinatorLayout.Behavior<BottomAppBar>(
    context, attributeSet
) {

    private var childHeight by Delegates.notNull<Float>()

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: BottomAppBar,
        layoutDirection: Int
    ): Boolean {
        childHeight = child.height.toFloat()
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: BottomAppBar,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL

    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: BottomAppBar,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (dyConsumed > 0) slideDown(child)
        else if (dyConsumed < 0) slideUp(child)
    }

    private fun slideUp(bottomAppBar: BottomAppBar) {
        bottomAppBar.clearAnimation()
        bottomAppBar.animate().translationY(0F).duration = 200
    }
    private fun slideDown(bottomAppBar: BottomAppBar) {
        bottomAppBar.clearAnimation()
        bottomAppBar.animate().translationY(childHeight).duration = 200
    }
}