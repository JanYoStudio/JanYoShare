package com.janyo.janyoshare.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.janyo.janyoshare.util.TransferFileNotification
import com.mystery0.tools.Logs.Logs

class TransferCancelService : Service()
{
	private val TAG = "TransferCancelService"

	override fun onBind(intent: Intent): IBinder?
	{
		return null
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		TransferFileNotification.cancel(this, 0)
		Logs.i(TAG, "onStartCommand: 退出所有服务")
		stopSelf()
		return super.onStartCommand(intent, flags, startId)
	}
}
