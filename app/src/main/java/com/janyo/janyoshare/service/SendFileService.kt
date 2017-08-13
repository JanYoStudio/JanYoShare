package com.janyo.janyoshare.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Message
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.classes.TransferHeader
import com.janyo.janyoshare.handler.ErrorHandler
import com.janyo.janyoshare.util.FileTransferHandler
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.TransferFileNotification
import com.mystery0.tools.Logs.Logs
import java.util.concurrent.Executors

class SendFileService : Service()
{
	private val TAG = "SendFileService"
	private lateinit var errorHandler: ErrorHandler
	private val socketUtil = SocketUtil()
	private var index = 0
	private val singleThreadPool = Executors.newSingleThreadExecutor()

	private val runnable = Runnable {
		//传输请求头
		val transferHeader = TransferHeader()
		transferHeader.list = FileTransferHandler.getInstance().fileList
		socketUtil.createServerConnection(FileTransferHandler.getInstance().transferPort1)
		if (socketUtil.sendObject(transferHeader))
		{
			Logs.i(TAG, "onCreate: 请求头传输成功")
			sendFile(FileTransferHandler.getInstance().fileList[index])
		}
		else
		{
			Logs.e(TAG, "onCreate: 请求头传输失败")
		}
	}

	override fun onCreate()
	{
		Logs.i(TAG, "onCreate: 创建传输文件服务")
		errorHandler = ErrorHandler(this)
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
//				thread.start()

			}
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
		socketUtil.sendFile(this, transferFile, object : SocketUtil.FileTransferListener
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
				TransferFileNotification.notify(this@SendFileService, index, "start")
			}

			override fun onFinish()
			{
				Logs.i(TAG, "onFinish: " + FileTransferHandler.getInstance().currentFile!!.fileName)
				FileTransferHandler.getInstance().currentProgress = 100
				TransferFileNotification.done(this@SendFileService, index, FileTransferHandler.getInstance().currentFile!!)
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
					1 -> message.what = ErrorHandler.FILE_NOT_EXISTS
					else -> message.what = ErrorHandler.UNKNOWN_ERROR
				}
				errorHandler.sendMessage(message)
				TransferFileNotification.cancel(this@SendFileService)
			}
		})
	}
}
