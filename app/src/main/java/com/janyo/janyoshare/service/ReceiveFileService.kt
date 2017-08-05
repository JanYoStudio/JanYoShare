package com.janyo.janyoshare.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Message
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.classes.TransferHeader
import com.janyo.janyoshare.handler.ErrorHandler
import com.janyo.janyoshare.util.FileTransferHandler
import com.janyo.janyoshare.util.JYFileUtil
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.TransferFileNotification
import com.mystery0.tools.Logs.Logs

class ReceiveFileService : Service()
{
	private val TAG = "ReceiveFileService"
	//	private lateinit var localBroadcastManager: LocalBroadcastManager
	private lateinit var errorHandler: ErrorHandler
	private val socketUtil = SocketUtil()
	private var index = 0

	private val thread = Thread(Runnable {
		socketUtil.createSocketConnection(FileTransferHandler.getInstance().ip, FileTransferHandler.getInstance().transferPort)
		//获取请求头
		val obj = socketUtil.receiveObject()
		if (obj != null)
		{
			val transferHeader = obj as TransferHeader
			FileTransferHandler.getInstance().fileList = transferHeader.list
			Logs.i(TAG, "onCreate: 获取请求头成功")
			receiveFile(FileTransferHandler.getInstance().fileList[index])
		}
		else
		{
			Logs.e(TAG, "onCreate: 获取请求头失败")
		}
	})

	override fun onCreate()
	{
		Logs.i(TAG, "onCreate: 创建接收文件服务")
		errorHandler = ErrorHandler(this)
//		//注册本地广播
//		localBroadcastManager = LocalBroadcastManager.getInstance(this)
//		val intentFilter = IntentFilter()
//		intentFilter.addAction(getString(R.string.com_janyo_janyoshare_UPDATE_PROGRESS))
//		localBroadcastManager.registerReceiver(FileTransferReceiver(), intentFilter)
	}

	override fun onBind(intent: Intent): IBinder?
	{
		return null
	}

	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
	{
		thread.start()
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onDestroy()
	{
		socketUtil.clientDisconnect()
		Logs.i(TAG, "onDestroy: ")
	}

	private fun receiveFile(transferFile: TransferFile)
	{
		val path = JYFileUtil.getSaveFilePath(transferFile.fileName!!, getString(R.string.app_name))
//		val broadcastIntent = Intent(getString(R.string.com_janyo_janyoshare_UPDATE_PROGRESS))
		socketUtil.receiveFile(transferFile.fileSize, path, object : SocketUtil.FileTransferListener
		{
			override fun onStart()
			{
				Logs.i(TAG, "onStart: ")
				FileTransferHandler.getInstance().currentFile = transferFile
				FileTransferHandler.getInstance().currentProgress = 0
				TransferFileNotification.notify(this@ReceiveFileService, index, "start")
			}

			override fun onProgress(progress: Int)
			{
				FileTransferHandler.getInstance().currentProgress = progress
				TransferFileNotification.notify(this@ReceiveFileService, index, "start")
			}

			override fun onFinish()
			{
				Logs.i(TAG, "onFinish: " + FileTransferHandler.getInstance().currentFile!!.fileName)
				FileTransferHandler.getInstance().currentProgress = 100
//				broadcastIntent.putExtra("index", index)
//				localBroadcastManager.sendBroadcast(broadcastIntent)
				TransferFileNotification.done(this@ReceiveFileService, index, FileTransferHandler.getInstance().currentFile!!)
//				val list = FileTransferHandler.getInstance().fileList
//				index++
//				if (index < list.size)
//					receiveFile(list[index])
//				else
				FileTransferHandler.getInstance().clear()
				stopSelf()
			}

			override fun onError(code: Int, e: Exception)
			{
				Logs.e(TAG, "onError: code: " + code)
				e.printStackTrace()
				val message = Message()
				when (code)
				{
					1 -> message.what = ErrorHandler.FILE_EXISTS
					else -> message.what = ErrorHandler.UNKNOWN_ERROR
				}
				errorHandler.sendMessage(message)
//				index++
			}
		})
	}
}
