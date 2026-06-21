package com.example.nutriguideproject.ui.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Line chart sederhana untuk menampilkan tren beberapa seri data.
 */
class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Series(val values: FloatArray, val color: Int)

    private var seriesList: List<Series> = emptyList()
    private var labels: Array<String> = arrayOf()
    private var maxValue: Float = 0f
    private val gridLines = 5

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE6E9EB.toInt()
        strokeWidth = dp(1f)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF9AA6AC.toInt()
        textSize = sp(10f)
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    fun setData(seriesList: List<Series>, labels: Array<String>, maxValue: Float) {
        this.seriesList = seriesList
        this.labels = labels
        this.maxValue = maxValue
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (seriesList.isEmpty() || maxValue <= 0f) return

        val leftPad = dp(34f)
        val bottomPad = dp(22f)
        val topPad = dp(8f)
        val plotLeft = leftPad
        val plotTop = topPad
        val plotRight = width - dp(8f)
        val plotBottom = height - bottomPad
        val plotHeight = plotBottom - plotTop
        val plotWidth = plotRight - plotLeft

        // Grid + y labels
        textPaint.textAlign = Paint.Align.RIGHT
        for (i in 0 until gridLines) {
            val frac = i / (gridLines - 1f)
            val y = plotBottom - frac * plotHeight
            canvas.drawLine(plotLeft, y, plotRight, y, gridPaint)
            val labelValue = (maxValue * frac).toInt()
            canvas.drawText(labelValue.toString(), plotLeft - dp(6f), y + sp(3.5f), textPaint)
        }

        val count = labels.size.coerceAtLeast(1)
        val stepX = if (count > 1) plotWidth / (count - 1) else plotWidth

        // X labels
        textPaint.textAlign = Paint.Align.CENTER
        for (i in labels.indices) {
            val x = plotLeft + stepX * i
            canvas.drawText(labels[i], x, plotBottom + dp(15f), textPaint)
        }

        // Series lines
        for (series in seriesList) {
            linePaint.color = series.color
            pointPaint.color = series.color
            var prevX = 0f
            var prevY = 0f
            for (i in series.values.indices) {
                val x = plotLeft + stepX * i
                val y = plotBottom - (series.values[i] / maxValue) * plotHeight
                if (i > 0) canvas.drawLine(prevX, prevY, x, y, linePaint)
                prevX = x
                prevY = y
            }
            for (i in series.values.indices) {
                val x = plotLeft + stepX * i
                val y = plotBottom - (series.values[i] / maxValue) * plotHeight
                canvas.drawCircle(x, y, dp(3f), pointPaint)
            }
        }
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density
    private fun sp(value: Float) = value * resources.displayMetrics.scaledDensity
}
