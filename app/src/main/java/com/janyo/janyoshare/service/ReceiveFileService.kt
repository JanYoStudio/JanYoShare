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
import com.janyo.janyoshare.util.JYFileUtil
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.TransferFileNotification
import com.mystery0.tools.Logs.Logs

class ReceiveFileService : Service()
{
	private val TAG = "ReceiveFileService"
	private var localBroadcastManager: LocalBroadcastManager? = null
	private val socketUtil = FileTransferHandler.getInstance().socketUtil
	private var index = 0

	private val thread = Thread(Runnable {
		receiveFile(FileTransferHandler.getInstance().fileList[index])
	})

	override fun onCreate()
	{
		Logs.i(TAG, "onCreate: 创建接收文件服务")
		//注册本地广播
		localBroadcastManager = LocalBroadcastManager.getInstance(this)
		val intentFilter = IntentFilter()
		intentFilter.addAction(getString(R.string.com_janyo_janyoshare_UPDATE_PROGRESS))
		localBroadcastManager!!.registerReceiver(FileTransferReceiver(), intentFilter)
		//获取请求头
		val obj = FileTransferHandler.getInstance().socketUtil!!.receiveObject()
		if (obj != null)
		{
			val transferHeader = obj as TransferHeader
			FileTransferHandler.getInstance().fileList = transferHeader.list
			Logs.i(TAG, "onCreate: 获取请求头成功")
		}
		else
		{
			Logs.e(TAG, "onCreate: 获取请求头失败")
		}
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
			"notify" ->
			{
				thread.start()
			}
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onDestroy()
	{

	}

	private fun receiveFile(transferFile: TransferFile)
	{
		val path = JYFileUtil.getSaveFilePath(transferFile.fileName!!, getString(R.string.app_name))
		socketUtil!!.receiveFile(transferFile.fileSize, path, object : SocketUtil.FileTransferListener
		{
			override fun onStart()
			{
				FileTransferHandler.getInstance().currentFile = transferFile
				FileTransferHandler.getInstance().currentProgress = 0
				TransferFileNotification.notify(this@ReceiveFileService, index, "start")
			}

			override fun onProgress(progress: Int)
			{
				val broadcastIntent = Intent(getString(R.string.com_janyo_janyoshare_UPDATE_PROGRESS))
				broadcastIntent.putExtra("progress", FileTransferHandler.getInstance().currentProgress)
				broadcastIntent.putExtra("index", index)
				localBroadcastManager!!.sendBroadcast(broadcastIntent)
				TransferFileNotification.notify(this@ReceiveFileService, index, "start")
			}

			override fun onFinish()
			{
				Logs.i(TAG, "onFinish: " + FileTransferHandler.getInstance().currentFile!!.fileName)
				val list = FileTransferHandler.getInstance().fileList
				if (index == list.size)
					receiveFile(list[++index])
			}

			override fun onError(code: Int, e: Exception)
			{
				e.printStackTrace()
				index++
			}
		})
	}
}
