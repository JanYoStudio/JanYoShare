package com.janyo.janyoshare.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import com.janyo.janyoshare.FileTransferReceiver
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.classes.TransferHeader
import com.janyo.janyoshare.util.FileTransferHandler
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.TransferFileNotification
import com.mystery0.tools.Logs.Logs
import java.io.File

class SendFileService : Service()
{
	private val TAG = "SendFileService"
	private lateinit var localBroadcastManager: LocalBroadcastManager
	private val socketUtil = SocketUtil()
	private var index = 0

	private val thread = Thread(Runnable {
		//传输请求头
		val transferHeader = TransferHeader()
		transferHeader.list = FileTransferHandler.getInstance().fileList
		if (socketUtil.sendObject(transferHeader))
		{
			Logs.i(TAG, "onCreate: 请求头传输成功")
			sendFile(FileTransferHandler.getInstance().fileList[index])
		}
		else
		{
			Logs.e(TAG, "onCreate: 请求头传输失败")
		}
	})

	override fun onCreate()
	{
		Logs.i(TAG, "onCreate: 创建传输文件服务")
		//注册本地广播
		localBroadcastManager = LocalBroadcastManager.getInstance(this)
		val intentFilter = IntentFilter()
		intentFilter.addAction(getString(R.string.com_janyo_janyoshare_UPDATE_PROGRESS))
		localBroadcastManager.registerReceiver(FileTransferReceiver(), intentFilter)
		Thread(Runnable {
			socketUtil.createServerConnection(FileTransferHandler.getInstance().transferPort)
		}).start()
	}

	override fun onBind(intent: Intent): IBinder?
	{
		return null
	}

	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
	{
		Logs.i(TAG, "onStartCommand: ")
		val action = intent.getStringExtra("action")
		when (action)
		{
			"start" ->
			{
				thread.start()
			}
//			"pause" ->
//			{
//
//			}
//			"stop" ->
//			{
//
//			}
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onDestroy()
	{
		socketUtil.clientDisconnect()
		Logs.i(TAG, "onDestroy: ")
	}

	private fun sendFile(transferFile: TransferFile)
	{
//		val broadcastIntent = Intent(getString(R.string.com_janyo_janyoshare_UPDATE_PROGRESS))
		socketUtil.sendFile(File(transferFile.filePath), object : SocketUtil.FileTransferListener
		{
			override fun onStart()
			{
				Logs.i(TAG, "onStart: ")
				FileTransferHandler.getInstance().currentProgress = 0
				FileTransferHandler.getInstance().currentFile = transferFile
				TransferFileNotification.notify(this@SendFileService, index, "start")
			}

			override fun onProgress(progress: Int)
			{
				FileTransferHandler.getInstance().currentProgress = progress
//				broadcastIntent.putExtra("index", index)
//				localBroadcastManager.sendBroadcast(broadcastIntent)
				TransferFileNotification.notify(this@SendFileService, index, "start")
			}

			override fun onFinish()
			{
				Logs.i(TAG, "onFinish: " + FileTransferHandler.getInstance().currentFile!!.fileName)
				FileTransferHandler.getInstance().currentProgress = 100
//				broadcastIntent.putExtra("index", index)
//				localBroadcastManager.sendBroadcast(broadcastIntent)
				TransferFileNotification.done(this@SendFileService,index)
				val list = FileTransferHandler.getInstance().fileList
				index++
				if (index < list.size)
					sendFile(list[index])
				else
					stopSelf()
			}

			override fun onError(code: Int, e: Exception)
			{
				Logs.e(TAG, "onError: ")
				e.printStackTrace()
				index++
			}
		})
	}
}
