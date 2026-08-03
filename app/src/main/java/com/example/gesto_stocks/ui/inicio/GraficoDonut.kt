package com.example.gesto_stocks.ui.inicio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/** Gráfico circular (donut) simples: desenha um arco por cada valor. */
class GraficoDonut @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private var fatias: List<Pair<Double, Int>> = emptyList()

    fun mostrar(valores: List<Double>, cores: List<Int>) {
        fatias = valores.mapIndexed { i, v -> v to cores[i % cores.size] }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val total = fatias.sumOf { it.first }
        if (total <= 0) return

        val espessura = minOf(width, height) / 5f
        paint.strokeWidth = espessura
        val meio = espessura / 2
        val rect = RectF(meio, meio, width - meio, height - meio)

        var angulo = -90f
        for ((valor, cor) in fatias) {
            val varrimento = (valor / total * 360).toFloat()
            paint.color = cor
            // -2 graus deixa uma pequena folga entre fatias, como no mockup
            canvas.drawArc(rect, angulo, (varrimento - 2f).coerceAtLeast(1f), false, paint)
            angulo += varrimento
        }
    }
}
