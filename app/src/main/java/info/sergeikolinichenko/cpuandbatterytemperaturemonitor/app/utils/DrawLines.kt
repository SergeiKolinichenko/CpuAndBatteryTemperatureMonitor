package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView

// Draws a line on the view
class DrawLines(context: Context, attrs: AttributeSet): MaterialTextView(context, attrs) {

    private val rect = Rect()
    private val paint = Paint()
    init {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = STROKE_WIDTH
        paint.color = context.getColorResCompat(android.R.attr.textColorPrimary)
    }

    override fun onDraw(canvas: Canvas) {

        var count = height/lineHeight

        if (lineCount > count) count = lineCount

        var baseline = getLineBounds(0, rect).toFloat()
        val fLeftRect = rect.left.toFloat()
        val fRightRect = rect.right.toFloat()

        for (i in  0..count) {
            canvas.drawLine(fLeftRect, baseline, fRightRect, baseline, paint)
            baseline += lineHeight
        }
        super.onDraw(canvas)
    }

    @ColorInt
    private fun Context.getColorResCompat(@AttrRes id: Int): Int {
        val resolvedAttr = TypedValue()
        this.theme.resolveAttribute(id, resolvedAttr, true)
        val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
        return ContextCompat.getColor(this, colorRes)
    }

    companion object{
        private const val STROKE_WIDTH = 2f
    }
}