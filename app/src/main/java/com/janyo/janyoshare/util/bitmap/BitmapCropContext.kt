package com.janyo.janyoshare.util.bitmap

import android.graphics.Bitmap

/**
 * Created by mystery0.
 */
class BitmapCropContext
{
	private lateinit var bitmapCrop: BitmapCrop

	companion object
	{
		val SQUARE = 1
		val CIRCLE = 2
	}

	fun setCropType(type: Int)
	{
		bitmapCrop = when (type)
		{
			1 -> SquareBitmapCrop()
			2 -> CircleBitmapCrop()
			else -> throw NullPointerException("the type is null")
		}
	}

	fun crop(bitmap: Bitmap?): Bitmap
	{
		if (bitmap == null)
			throw NullPointerException("bitmap can not be null")

		return bitmapCrop.crop(bitmap)
	}
}