package com.example.studybuddy.ui.timer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.studybuddy.R

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        color = ContextCompat.getColor(context, R.color.light_gray)
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        color = ContextCompat.getColor(context, R.color.colorAccent)
        strokeCap = Paint.Cap.ROUND
    }

    private val rectF = RectF()
    private var progress = 100f

    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 100f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (Math.min(width, height) / 2f) - backgroundPaint.strokeWidth

        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Draw progress arc
        val sweepAngle = (progress / 100f) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = Math.min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }
}