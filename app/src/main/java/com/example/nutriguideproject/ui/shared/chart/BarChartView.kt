package com.example.nutriguideproject.ui.shared.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * Bar chart sederhana untuk menampilkan asupan kalori per hari.
 * Diisi lewat [setData]. Tidak memakai library eksternal.
 */
class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var values: FloatArray = floatArrayOf()
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
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setData(values: FloatArray, labels: Array<String>, maxValue: Float) {
        this.values = values
        this.labels = labels
        this.maxValue = maxValue
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty() || maxValue <= 0f) return

        val leftPad = dp(34f)
        val bottomPad = dp(22f)
        val topPad = dp(8f)
        val plotLeft = leftPad
        val plotTop = topPad
        val plotRight = width - dp(8f)
        val plotBottom = height - bottomPad
        val plotHeight = plotBottom - plotTop
        val plotWidth = plotRight - plotLeft

        // Grid lines + y labels
        textPaint.textAlign = Paint.Align.RIGHT
        for (i in 0 until gridLines) {
            val frac = i / (gridLines - 1f)
            val y = plotBottom - frac * plotHeight
            canvas.drawLine(plotLeft, y, plotRight, y, gridPaint)
            val labelValue = (maxValue * frac).toInt()
            canvas.drawText(labelValue.toString(), plotLeft - dp(6f), y + sp(3.5f), textPaint)
        }

        // Bars
        val slot = plotWidth / values.size
        val barWidth = slot * 0.5f
        textPaint.textAlign = Paint.Align.CENTER
        for (i in values.indices) {
            val cx = plotLeft + slot * i + slot / 2f
            val barHeight = (values[i] / maxValue) * plotHeight
            val barTop = plotBottom - barHeight
            val rect = RectF(cx - barWidth / 2f, barTop, cx + barWidth / 2f, plotBottom)
            barPaint.shader = LinearGradient(
                0f, barTop, 0f, plotBottom,
                0xFF8CC08A.toInt(), 0xFF3E6E72.toInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(rect, dp(4f), dp(4f), barPaint)
            if (i < labels.size) {
                canvas.drawText(labels[i], cx, plotBottom + dp(15f), textPaint)
            }
        }
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density
    private fun sp(value: Float) = value * resources.displayMetrics.scaledDensity
}
