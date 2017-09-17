package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build

/**
 * Created by mystery0.
 */
class AdaptiveIconDrawableConvert : DrawableConvert()
{
	override fun Convert(drawable: Drawable): Bitmap?
	{
		val adaptiveIconDrawable = drawable as AdaptiveIconDrawable
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
		{
			return null
		}
		val layerDrawable = LayerDrawable(arrayOf(adaptiveIconDrawable.background, adaptiveIconDrawable.foreground))
		val bitmap = Bitmap.createBitmap(layerDrawable.intrinsicWidth, layerDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		layerDrawable.setBounds(0, 0, canvas.width, canvas.height)
		layerDrawable.draw(canvas)
		return bitmap
	}
}