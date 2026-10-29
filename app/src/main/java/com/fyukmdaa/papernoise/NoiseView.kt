package com.fyukmdaa.papernoise

import android.content.Context
import android.graphics.*
import android.view.View

class NoiseView(c: Context) : View(c) {
    private val noise = Paint()
    private val warm = Paint()
    private val shader: BitmapShader

    init {
        val n = 128
        val r = java.util.Random(7)
        val px = IntArray(n * n) {
            val a = r.nextInt(256)
            if (r.nextBoolean()) Color.argb(a, 0, 0, 0) else Color.argb(a, 255, 248, 230)
        }
        val bmp = Bitmap.createBitmap(px, n, n, Bitmap.Config.ARGB_8888)
        shader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        noise.shader = shader
        noise.isFilterBitmap = false
    }

    /** 各値は 0..100 */
    fun update(intensity: Int, grain: Int, warmth: Int) {
        val s = 1f + grain / 100f * 3f
        shader.setLocalMatrix(Matrix().apply { setScale(s, s) })
        noise.alpha = (intensity / 100f * 0.35f * 255).toInt()
        warm.color = Color.argb((warmth / 100f * 0.3f * 255).toInt(), 255, 190, 110)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPaint(warm)
        canvas.drawPaint(noise)
    }
}
