package com.janyo.janyoshare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.janyo.janyoshare.util.FileTransferHandler
import com.janyo.janyoshare.util.TransferFileNotification
import com.mystery0.tools.Logs.Logs

class FileTransferReceiver : BroadcastReceiver()
{
	private val TAG = "FileTransferReceiver"

	override fun onReceive(context: Context, intent: Intent)
	{
		val progress = intent.getIntExtra("progress", 0)
		val index = intent.getIntExtra("index", 0)
		Logs.i(TAG, "onReceive: " + progress)
		FileTransferHandler.getInstance().currentProgress = progress
		TransferFileNotification.notify(context, index, "start")
	}
}
