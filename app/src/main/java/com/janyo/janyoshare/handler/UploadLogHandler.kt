package com.janyo.janyoshare.handler

import android.app.Activity
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.view.View
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.Response
import vip.mystery0.tools.Logs.Logs
import dmax.dialog.SpotsDialog

class UploadLogHandler : Handler()
{
	private val TAG = "UploadLogHandler"
	lateinit var coordinatorLayout: View
	lateinit var spotsDialog: SpotsDialog
	lateinit var activity: Activity
	var response: Response? = null

	override fun handleMessage(msg: Message)
	{
		spotsDialog.dismiss()
		val code = response?.code ?: -1
		if (code == 0)
		{
			Snackbar.make(coordinatorLayout, R.string.hint_upload_log_done, Snackbar.LENGTH_SHORT)
					.addCallback(object : Snackbar.Callback()
					{
						override fun onDismissed(transientBottomBar: Snackbar?, event: Int)
						{
							activity.finish()
						}
					})
					.show()
		}
		else
		{
			Logs.e(TAG, "onResponse: " + response?.message)
			Snackbar.make(coordinatorLayout, R.string.hint_upload_log_error, Snackbar.LENGTH_SHORT)
					.addCallback(object : Snackbar.Callback()
					{
						override fun onDismissed(transientBottomBar: Snackbar?, event: Int)
						{
							activity.finish()
						}
					})
					.show()
		}
	}
}