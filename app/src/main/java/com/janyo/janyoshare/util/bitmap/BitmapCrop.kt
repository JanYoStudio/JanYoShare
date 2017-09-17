package com.janyo.janyoshare.util.bitmap

import android.graphics.Bitmap

/**
 * Created by mystery0.
 */
abstract class BitmapCrop
{
	abstract fun crop(bitmap: Bitmap): Bitmap
}