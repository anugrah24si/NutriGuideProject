package com.example.nutriguideproject.ui.shared.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Pie chart sederhana dengan label persentase di sekitar tiap irisan.
 */
class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Slice(val label: String, val value: Float, val color: Int)

    private var slices: List<Slice> = emptyList()

    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(11f)
    }

    fun setData(slices: List<Slice>) {
        this.slices = slices
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (slices.isEmpty()) return
        val total = slices.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0f) return

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f - dp(40f)
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        var startAngle = -90f
        for (slice in slices) {
            val sweep = slice.value / total * 360f
            slicePaint.color = slice.color
            canvas.drawArc(rect, startAngle, sweep, true, slicePaint)

            // Label at mid-angle, just outside the pie
            val midAngle = Math.toRadians((startAngle + sweep / 2f).toDouble())
            val lx = cx + (radius + dp(18f)) * cos(midAngle).toFloat()
            val ly = cy + (radius + dp(18f)) * sin(midAngle).toFloat()
            val percent = Math.round(slice.value / total * 100f)
            labelPaint.color = slice.color
            labelPaint.textAlign = if (cos(midAngle) >= 0) Paint.Align.LEFT else Paint.Align.RIGHT
            canvas.drawText("${slice.label} $percent%", lx, ly, labelPaint)

            startAngle += sweep
        }
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density
    private fun sp(value: Float) = value * resources.displayMetrics.scaledDensity
}
