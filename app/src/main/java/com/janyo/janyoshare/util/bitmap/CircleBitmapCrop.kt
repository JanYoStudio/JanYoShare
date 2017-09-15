package com.janyo.janyoshare.util.bitmap

import android.graphics.*

/**
 * Created by mystery0.
 */
class CircleBitmapCrop:BitmapCrop()
{
	override fun crop(bitmap: Bitmap): Bitmap
	{
		var width = bitmap.width
		var height = bitmap.height
		val roundPx: Float//半径
		val left: Float
		val top: Float
		val right: Float
		val bottom: Float
		val dst_left: Float
		val dst_top: Float
		val dst_right: Float
		val dst_bottom: Float
		if (width <= height)
		{
			roundPx = (width / 2).toFloat()
			top = 0f
			bottom = width.toFloat()
			left = 0f
			right = width.toFloat()
			height = width
			dst_left = 0f
			dst_top = 0f
			dst_right = width.toFloat()
			dst_bottom = width.toFloat()
		}
		else
		{
			roundPx = (height / 2).toFloat()
			val clip = ((width - height) / 2).toFloat()
			left = clip
			right = width - clip
			top = 0f
			bottom = height.toFloat()
			width = height
			dst_left = 0f
			dst_top = 0f
			dst_right = height.toFloat()
			dst_bottom = height.toFloat()
		}

		val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(output)

		val color = 0xff424242.toInt()
		val paint = Paint()
		val src = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
		val dst = Rect(dst_left.toInt(), dst_top.toInt(), dst_right.toInt(), dst_bottom.toInt())
		val rectF = RectF(dst)

		paint.isAntiAlias = true

		canvas.drawARGB(0, 0, 0, 0)
		paint.color = color
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

		paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
		canvas.drawBitmap(bitmap, src, dst, paint)
		return output
	}
}