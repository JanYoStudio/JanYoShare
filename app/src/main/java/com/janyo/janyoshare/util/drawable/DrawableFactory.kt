package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import com.janyo.janyoshare.util.Settings
import com.janyo.janyoshare.util.bitmap.BitmapCropContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by mystery0.
 */
class DrawableFactory(settings: Settings)
{
	private val drawableConvertContext = DrawableConvertContext()
	private val bitmapCropContext = BitmapCropContext()

	init
	{
		when (settings.iconCropType)
		{
			1 -> bitmapCropContext.setCropType(BitmapCropContext.ROUND)
			2 -> bitmapCropContext.setCropType(BitmapCropContext.RECTANGLE)
			3 -> bitmapCropContext.setCropType(BitmapCropContext.ROUND_RECTANGLE)
			else -> bitmapCropContext.setCropType(BitmapCropContext.DEFAULT)
		}
	}

	fun save(drawable: Drawable, path: String): Boolean
	{
		try
		{
			val file = File(path)
			if (!file.parentFile.exists())
			{
				file.parentFile.mkdirs()
			}
			val out = FileOutputStream(file)
			var bitmap = drawableConvertContext.convert(drawable) ?: return false
			if (drawable is AdaptiveIconDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				bitmap = bitmapCropContext.crop(bitmap)
			}
			bitmap.compress(Bitmap.CompressFormat.PNG, 1, out)
			out.close()
		}
		catch (e: IOException)
		{
			e.printStackTrace()
			return false
		}
		return true
	}
}