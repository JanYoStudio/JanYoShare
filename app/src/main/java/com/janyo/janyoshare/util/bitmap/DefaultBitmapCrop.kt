package com.janyo.janyoshare.util.bitmap

import android.graphics.Bitmap

/**
 * Created by mystery0.
 */
class DefaultBitmapCrop : BitmapCrop()
{
	override fun crop(bitmap: Bitmap): Bitmap
	{
		return bitmap
	}

}