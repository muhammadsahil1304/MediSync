package com.example.newmedisync.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    init {
    }
    data class Stroke(
        val path: Path,
        val paint: Paint
    )

    private val strokes = mutableListOf<Stroke>()

    private var currentPath = Path()

    private var currentPaint = createPaint(
        Color.parseColor("#0A70A2")
    )

    private var isEraser = false

    private fun createPaint(color: Int): Paint {

        return Paint().apply {

            this.color = color

            isAntiAlias = true

            style = Paint.Style.STROKE

            strokeJoin = Paint.Join.ROUND

            strokeCap = Paint.Cap.ROUND

            strokeWidth = 8f
        }
    }

    fun undoLastStroke() {

        if (strokes.isNotEmpty()) {

            strokes.removeAt(strokes.lastIndex)

            invalidate()
        }
    }

    fun setBrushColor(color: Int) {

        isEraser = false

        currentPaint = createPaint(color)
    }

    fun enableEraser() {

        isEraser = true

        currentPaint = Paint().apply {

            color = Color.WHITE

            isAntiAlias = true

            style = Paint.Style.STROKE

            strokeJoin = Paint.Join.ROUND

            strokeCap = Paint.Cap.ROUND

            strokeWidth = 60f
        }
    }

    fun clearCanvas() {

        strokes.clear()

        currentPath.reset()

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        for (stroke in strokes) {

            canvas.drawPath(
                stroke.path,
                stroke.paint
            )
        }

        canvas.drawPath(
            currentPath,
            currentPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        val x = event.x
        val y = event.y

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                currentPath = Path()

                currentPath.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {

                currentPath.lineTo(x, y)
            }

            MotionEvent.ACTION_UP -> {

                strokes.add(
                    Stroke(
                        Path(currentPath),
                        Paint(currentPaint)
                    )
                )

                currentPath = Path()
            }
        }

        invalidate()

        return true
    }

    fun getStrokes(): MutableList<Stroke> {
        return strokes
    }

    fun setStrokes(
        newStrokes: MutableList<Stroke>
    ) {

        strokes.clear()

        strokes.addAll(newStrokes)

        invalidate()
    }
}