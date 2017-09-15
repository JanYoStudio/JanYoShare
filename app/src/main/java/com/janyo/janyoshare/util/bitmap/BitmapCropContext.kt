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
		val DEFAULT = 0
		val CIRCLE = 1
		val SQUARE = 2
	}

	fun setCropType(type: Int)
	{
		bitmapCrop = when (type)
		{
			0 -> DefaultBitmapCrop()
			1 -> CircleBitmapCrop()
			2 -> SquareBitmapCrop()
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