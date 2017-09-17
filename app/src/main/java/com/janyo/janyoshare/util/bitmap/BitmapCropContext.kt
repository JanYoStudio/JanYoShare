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
		val ROUND = 1
		val RECTANGLE = 2
		val ROUND_RECTANGLE = 3
	}

	fun setCropType(type: Int)
	{
		bitmapCrop = when (type)
		{
			DEFAULT -> DefaultBitmapCrop()
			ROUND -> RoundBitmapCrop()
			RECTANGLE -> RectangleBitmapCrop()
			ROUND_RECTANGLE -> RoundRectangleCrop()
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