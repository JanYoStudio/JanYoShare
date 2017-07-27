package com.janyo.janyoshare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.janyo.janyoshare.util.FileTransferHandler
import com.janyo.janyoshare.util.TransferFileNotification

class FileTransferReceiver : BroadcastReceiver()
{
	override fun onReceive(context: Context, intent: Intent)
	{
		val index = intent.getIntExtra("index", 0)
		TransferFileNotification.notify(context, index, "start")
	}
}
