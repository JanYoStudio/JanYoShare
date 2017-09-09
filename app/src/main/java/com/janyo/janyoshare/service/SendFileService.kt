package com.janyo.janyoshare.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Message
import com.janyo.janyoshare.classes.TransferHeader
import com.janyo.janyoshare.handler.ErrorHandler
import com.janyo.janyoshare.handler.TransferHelperHandler
import com.janyo.janyoshare.util.FileTransferHelper
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.TransferFileNotification
import vip.mystery0.tools.Logs.Logs
import java.util.concurrent.Executors

class SendFileService : Service()
{
	private val TAG = "SendFileService"
	private lateinit var errorHandler: ErrorHandler
	private lateinit var transferHelperHandler: TransferHelperHandler
	private val socketUtil = SocketUtil()
	private val singleHeaderThreadPool = Executors.newSingleThreadExecutor()
	private val singleFileThreadPool = Executors.newSingleThreadExecutor()

	override fun onCreate()
	{
		Logs.i(TAG, "onCreate: 创建传输文件服务")
		errorHandler = ErrorHandler(this)
		if (FileTransferHelper.getInstance().transferHelperHandler != null)
			transferHelperHandler = FileTransferHelper.getInstance().transferHelperHandler!!
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
				singleHeaderThreadPool.execute {
					//传输请求头
					val transferHeader = TransferHeader()
					transferHeader.list = FileTransferHelper.getInstance().fileList
					socketUtil.createServerConnection(FileTransferHelper.getInstance().transferPort)
					if (socketUtil.sendObject(transferHeader))
					{
						Logs.i(TAG, "onCreate: 请求头传输成功")
						FileTransferHelper.getInstance().fileList.forEachIndexed { index, transferFile ->
							Logs.i(TAG, "onStartCommand: " + transferFile.fileName)
							singleFileThreadPool.execute {
								socketUtil.sendFile(this, transferFile, object : SocketUtil.FileTransferListener
								{
									override fun onStart()
									{
										Logs.i(TAG, "onStart: ")
										FileTransferHelper.getInstance().currentFileIndex = index
										transferFile.transferProgress = 0
										TransferFileNotification.notify(this@SendFileService, index)
										val message = Message()
										message.what = TransferHelperHandler.UPDATE_UI
										transferHelperHandler.sendMessage(message)
									}

									override fun onProgress(progress: Int)
									{
										Logs.i(TAG, "onProgress: " + progress)
										transferFile.transferProgress = progress
										TransferFileNotification.notify(this@SendFileService, index)
										val message = Message()
										message.what = TransferHelperHandler.UPDATE_UI
										transferHelperHandler.sendMessage(message)
									}

									override fun onFinish()
									{
										Logs.i(TAG, "onFinish: " + transferFile.fileName)
										transferFile.transferProgress = 100
										TransferFileNotification.done(this@SendFileService, index, transferFile)
										val map = HashMap<String, Any>()
										map.put("context", this@SendFileService)
										map.put("fileName", transferFile.fileName!!)
										val message = Message()
										message.obj = map
										message.what = TransferHelperHandler.UPDATE_TOAST
										transferHelperHandler.sendMessage(message)
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
										TransferFileNotification.cancel(this@SendFileService, index)
									}
								})
							}
							Thread.sleep(1000)
						}
						singleFileThreadPool.shutdown()
						while (true)
						{
							if (singleFileThreadPool.isTerminated)
							{
								Logs.i(TAG, "onStartCommand: 执行完毕")
								FileTransferHelper.getInstance().fileList
										.filter { it.transferProgress == 100 }
										.forEachIndexed { index, transferFile ->
											TransferFileNotification.done(this@SendFileService, index, transferFile)
										}
								FileTransferHelper.getInstance().clear()
								stopSelf()
								break
							}
							Thread.sleep(500)
						}
					}
					else
					{
						Logs.e(TAG, "onCreate: 请求头传输失败")
						stopSelf()
					}
				}
			}
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onDestroy()
	{
		socketUtil.clientDisconnect()
		Logs.i(TAG, "onDestroy: ")
	}
}
