package com.janyo.janyoshare.util.bitmap

import android.graphics.Bitmap

/**
 * Created by mystery0.
 */
class SquareBitmapCrop : BitmapCrop()
{
	override fun crop(bitmap: Bitmap): Bitmap
	{
		val width = bitmap.width
		val height = bitmap.height

		val length = if (width > height) height else width

		val retX = if (width > height) (width - height) / 2 else 0
		val retY = if (width > height) 0 else (height - width) / 2

		return Bitmap.createBitmap(bitmap, retX, retY, length, length, null,
				false)
	}
}