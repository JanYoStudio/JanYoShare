package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.support.graphics.drawable.VectorDrawableCompat

/**
 * Created by mystery0.
 */
class DrawableConvertContext
{
	private lateinit var drawableConvert: DrawableConvert

	fun convert(drawable: Drawable): Bitmap?
	{
		drawableConvert = when (drawable)
		{
			is BitmapDrawable -> BitmapDrawableConvert()
			is VectorDrawableCompat -> VectorDrawableCompatConvert()
			is VectorDrawable -> VectorDrawableConvert()
			is AdaptiveIconDrawable -> AdaptiveIconDrawableConvert()
			else -> return null
		}
		return drawableConvert.Convert(drawable)
	}
}