package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

/**
 * Created by mystery0.
 */
abstract class DrawableConvert
{
	abstract fun Convert(drawable: Drawable): Bitmap?
}