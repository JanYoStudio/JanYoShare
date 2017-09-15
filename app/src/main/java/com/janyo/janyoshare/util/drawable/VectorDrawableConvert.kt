package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable

/**
 * Created by mystery0.
 */
class VectorDrawableConvert : DrawableConvert()
{
	override fun Convert(drawable: Drawable): Bitmap
	{
		val vectorDrawable = drawable as VectorDrawable
		val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
				vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
		vectorDrawable.draw(canvas)
		return bitmap
	}
}