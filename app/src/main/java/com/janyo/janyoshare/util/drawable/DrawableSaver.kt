package com.janyo.janyoshare.util.drawable

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by mystery0.
 */
class DrawableSaver private constructor()
{
	private val drawableConvertContext = DrawableConvertContext()

	companion object
	{
		private var drawableSaver: DrawableSaver? = null

		fun getInstance(): DrawableSaver
		{
			if (drawableSaver == null)
				drawableSaver = DrawableSaver()
			return drawableSaver!!
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
			val bitmap = drawableConvertContext.convert(drawable) ?: return false
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