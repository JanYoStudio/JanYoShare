package com.janyo.janyoshare.util.bitmap

import android.graphics.*

/**
 * Created by mystery0.
 */
class RoundBitmapCrop : BitmapCrop()
{
	override fun crop(bitmap: Bitmap): Bitmap
	{
		val width = bitmap.width
		val height = bitmap.height
		val round = if (width <= height) width * 88 / 192f else height * 88 / 192f
		val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(output)
		val paint = Paint()
		paint.isAntiAlias = true
		canvas.drawARGB(0, 0, 0, 0)
		canvas.drawCircle(width / 2f, height / 2f, round, paint)
		paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)//设置图像重叠时的处理方式
		canvas.drawBitmap(bitmap, 0f, 0f, paint)
		return output
	}
}