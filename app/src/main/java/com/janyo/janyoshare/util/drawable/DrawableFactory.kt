package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import com.janyo.janyoshare.APP
import com.janyo.janyoshare.util.Settings
import com.janyo.janyoshare.util.bitmap.BitmapCropContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by mystery0.
 */
class DrawableFactory private constructor()
{
	private val drawableConvertContext = DrawableConvertContext()
	private val settings = Settings.getInstance(APP.getInstance())

	companion object
	{
		private var drawableFactory: DrawableFactory? = null

		fun getInstance(): DrawableFactory
		{
			if (drawableFactory == null)
				drawableFactory = DrawableFactory()
			return drawableFactory!!
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
				val bitmapCropContext = BitmapCropContext()
				when (settings.iconCropType)
				{
					1 -> bitmapCropContext.setCropType(BitmapCropContext.CIRCLE)
					2 -> bitmapCropContext.setCropType(BitmapCropContext.SQUARE)
					else -> bitmapCropContext.setCropType(BitmapCropContext.DEFAULT)
				}
				bitmap = bitmapCropContext.crop(bitmap)
			}
			bitmap.compress(Bitmap.CompressFormat.PNG, 10, out)
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