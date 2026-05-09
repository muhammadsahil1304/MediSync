package com.example.newmedisync.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var drawPath = Path()

    private var drawPaint = Paint().apply {
        color = Color.parseColor("#0A70A2")
        isAntiAlias = true
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(drawPath, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        parent.requestDisallowInterceptTouchEvent(true)

        val touchX = event.x
        val touchY = event.y

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                drawPath.moveTo(touchX, touchY)
            }

            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX, touchY)
            }

            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }

            else -> return false
        }

        invalidate()
        return true
    }
    fun getDrawPath(): Path {
        return drawPath
    }
    fun setDrawPath(path: Path) {
        drawPath = path
        invalidate()
    }
    fun clearCanvas() {
        drawPath.reset()
        invalidate()
    }

    fun setBrushColor(color: Int) {
        drawPaint.color = color
    }
}