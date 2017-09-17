package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.graphics.drawable.VectorDrawableCompat

/**
 * Created by mystery0.
 */
class VectorDrawableCompatConvert : DrawableConvert()
{
	override fun Convert(drawable: Drawable): Bitmap
	{
		val vectorDrawableCompat = drawable as VectorDrawableCompat
		val bitmap = Bitmap.createBitmap(vectorDrawableCompat.intrinsicWidth,
				vectorDrawableCompat.intrinsicHeight, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		vectorDrawableCompat.setBounds(0, 0, canvas.width, canvas.height)
		vectorDrawableCompat.draw(canvas)
		return bitmap
	}
}