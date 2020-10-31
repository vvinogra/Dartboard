package com.github.vvinogra.dartboard

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

class DartBoard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    companion object {
        private val numbers = arrayOf(20, 5, 15, 10, 20, 5, 15, 10, 20, 5, 15, 10)

        private val sweepAngle = 360F / numbers.size

        private val paint = Paint()

        private const val redColor = 0xFFFE3206.toInt()
        private const val greenColor = 0xFF01AC4A.toInt()
        private const val blackColor = Color.BLACK
        private const val whiteColor = 0xFFF0DBBB.toInt()

        private val arcSegments = arrayOf(
            ArcPart(redColor, redColor, 0.04F),
            ArcPart(greenColor, greenColor, 0.08F),
            ArcPart(blackColor, whiteColor, 0.3F),
            ArcPart(redColor, greenColor, 0.34F),
            ArcPart(blackColor, whiteColor, 0.64F),
            ArcPart(redColor, greenColor, 0.68F),
            ArcPart(blackColor, blackColor, 1F)
        )

        private val textPaint = TextPaint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
    }

    private val minorCanvasSize: Float
        get() = (if (width < height) width else height).toFloat()

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var startAngle = 0F

        for (i in numbers.indices) {
            val isFirstPaint = i % 2 == 0

            drawFullSegment(startAngle, isFirstPaint, canvas, numbers[i].toString())

            startAngle += sweepAngle
        }
    }

    private fun drawFullSegment(
        startAngle: Float,
        isFirstColor: Boolean,
        canvas: Canvas,
        textToDraw: String
    ) {
        val centerPoint = minorCanvasSize / 2F
        val maxCircleRadius = minorCanvasSize / 2F

        for (i in arcSegments.indices) {
            val innerRadius: Float = if (i == 0) {
                0F
            } else {
                maxCircleRadius * arcSegments[i - 1].ratio
            }

            val outerRadius: Float = maxCircleRadius * arcSegments[i].ratio

            val selectedColor = arcSegments[i].run {
                return@run if (isFirstColor) firstColor else secondColor
            }

            paint.color = selectedColor

            drawArcPart(centerPoint, innerRadius, outerRadius, startAngle, canvas)

            if (i == arcSegments.lastIndex) {
                textPaint.textSize = getPreferredTextSize(innerRadius, outerRadius)
                val centerRadius = (outerRadius + innerRadius) / 2

                drawCenteredCurvedText(centerPoint, centerRadius, startAngle, textToDraw, canvas)
            }
        }
    }

    private fun drawArcPart(
        center: Float,
        iRadius: Float,
        outRadius: Float,
        startAngle: Float,
        canvas: Canvas
    ) {
        val innerCircle = RectF(
            center - iRadius, center - iRadius,
            center + iRadius, center + iRadius
        )
        val outerCircle = RectF(
            center - outRadius, center - outRadius,
            center + outRadius, center + outRadius
        )

        val startAngleRad = toRadians(startAngle.toDouble()).toFloat()

        path.moveTo(center + iRadius * cos(startAngleRad), center + iRadius * sin(startAngleRad))
        path.lineTo(
            center + outRadius * cos(startAngleRad),
            center + outRadius * sin(startAngleRad)
        )

        path.arcTo(outerCircle, startAngle, sweepAngle)

        val endAngleRad = toRadians((startAngle + sweepAngle).toDouble()).toFloat()
        path.lineTo(center + iRadius * cos(endAngleRad), center + iRadius * sin(endAngleRad))

        path.arcTo(innerCircle, startAngle + sweepAngle, -sweepAngle)

        canvas.drawPath(path, paint)

        path.reset()
    }

    private fun getPreferredTextSize(iRadius: Float, outRadius: Float): Float {
        val outerArcLength = (toRadians(sweepAngle.toDouble()) * outRadius).toFloat()

        val diffRadius = outRadius - iRadius

        return if (outerArcLength < diffRadius) {
            outerArcLength
        } else {
            diffRadius
        } / 2
    }

    private fun drawCenteredCurvedText(
        center: Float,
        radius: Float,
        startAngle: Float,
        textToDraw: String,
        canvas: Canvas
    ) {
        val startAngleRad = toRadians(startAngle.toDouble()).toFloat()

        val centeredRadius = radius - (textPaint.textSize / 2)

        path.moveTo(
            center + centeredRadius * cos(startAngleRad),
            center + centeredRadius * sin(startAngleRad)
        )

        val midCircle = RectF(
            center - centeredRadius, center - centeredRadius,
            center + centeredRadius, center + centeredRadius
        )

        path.arcTo(midCircle, startAngle, sweepAngle)

        canvas.drawTextOnPath(textToDraw, path, 0F, 0F, textPaint)

        path.reset()
    }

    private data class ArcPart(
        @ColorInt val firstColor: Int,
        @ColorInt val secondColor: Int,
        val ratio: Float
    )
}
