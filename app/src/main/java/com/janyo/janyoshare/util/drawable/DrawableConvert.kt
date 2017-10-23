package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

/**
 * Created by mystery0.
 */
abstract class DrawableConvert
{
	abstract fun convert(drawable: Drawable): Bitmap?
}