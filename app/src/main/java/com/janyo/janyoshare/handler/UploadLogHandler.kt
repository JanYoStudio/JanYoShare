package com.janyo.janyoshare.handler

import android.app.Activity
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.view.View
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.Response
import com.mystery0.tools.Logs.Logs
import dmax.dialog.SpotsDialog

class UploadLogHandler : Handler()
{
	private val TAG = "UploadLogHandler"
	lateinit var coordinatorLayout: View
	lateinit var progressDialog: SpotsDialog
	lateinit var activity: Activity
	lateinit var response: Response

	override fun handleMessage(msg: Message)
	{
		progressDialog.dismiss()
		if (response.code == 0)
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
			Logs.e(TAG, "onResponse: " + response.message)
		}
	}
}