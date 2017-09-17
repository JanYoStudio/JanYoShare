package com.janyo.janyoshare.handler

import android.os.Handler
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import com.janyo.janyoshare.R

/**
 * Created by myste.
 */
class DownloadQrcodeHandler(private val coordinatorLayout: CoordinatorLayout) : Handler()
{
	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			1 -> Snackbar.make(coordinatorLayout, R.string.hint_pay_save_img_error, Snackbar.LENGTH_SHORT)
					.show()
			2 -> Snackbar.make(coordinatorLayout, R.string.hint_pay_save_img_done, Snackbar.LENGTH_SHORT)
					.show()
		}
	}
}