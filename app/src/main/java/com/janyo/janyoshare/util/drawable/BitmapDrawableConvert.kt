package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

/**
 * Created by mystery0.
 */
class BitmapDrawableConvert : DrawableConvert()
{
	override fun convert(drawable: Drawable): Bitmap
	{
		return (drawable as BitmapDrawable).bitmap
	}
}