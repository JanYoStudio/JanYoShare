package com.janyo.janyoshare.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Message
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferHeader
import com.janyo.janyoshare.handler.ErrorHandler
import com.janyo.janyoshare.handler.TransferHelperHandler
import com.janyo.janyoshare.util.FileTransferHelper
import com.janyo.janyoshare.util.JYFileUtil
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.TransferFileNotification
import com.mystery0.tools.Logs.Logs
import java.util.concurrent.Executors

class ReceiveFileService : Service()
{
	private val TAG = "ReceiveFileService"
	private lateinit var errorHandler: ErrorHandler
	private var transferHelperHandler: TransferHelperHandler? = null
	private val socketUtil = SocketUtil()
	private val singleHeaderThreadPool = Executors.newSingleThreadExecutor()
	private val singleFileThreadPool = Executors.newSingleThreadExecutor()
	private val singleThreadPool = Executors.newSingleThreadExecutor()

	override fun onCreate()
	{
		Logs.i(TAG, "onCreate: 创建接收文件服务")
		errorHandler = ErrorHandler(this)
		singleThreadPool.execute {
			while (true)
			{
				if (FileTransferHelper.getInstance().transferHelperHandler != null)
				{
					transferHelperHandler = FileTransferHelper.getInstance().transferHelperHandler
					break
				}
				Thread.sleep(200)
			}
		}
	}

	override fun onBind(intent: Intent): IBinder?
	{
		return null
	}

	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
	{
		singleHeaderThreadPool.execute {
			socketUtil.createSocketConnection(FileTransferHelper.getInstance().ip, FileTransferHelper.getInstance().transferPort)
			//获取请求头
			val obj = socketUtil.receiveObject()
			if (obj != null)
			{
				val transferHeader = obj as TransferHeader
				FileTransferHelper.getInstance().fileList.clear()
				FileTransferHelper.getInstance().fileList.addAll(transferHeader.list)
				FileTransferHelper.getInstance().transferHelperHandler!!.list.clear()
				FileTransferHelper.getInstance().transferHelperHandler!!.list.addAll(transferHeader.list)
				Logs.i(TAG, "onCreate: 获取请求头成功")
				Logs.i(TAG, "onStartCommand: " + transferHeader.list.size)
				val updateListMessage = Message()
				updateListMessage.what = TransferHelperHandler.UPDATE_UI
				transferHelperHandler!!.sendMessage(updateListMessage)
				FileTransferHelper.getInstance().fileList.forEachIndexed { index, transferFile ->
					singleFileThreadPool.execute {
						Logs.i(TAG, "onStartCommand: " + transferFile.fileName)
						val path = JYFileUtil.getSaveFilePath(transferFile.fileName!!, getString(R.string.app_name))
						socketUtil.receiveFile(transferFile.fileSize, path, object : SocketUtil.FileTransferListener
						{
							override fun onStart()
							{
								Logs.i(TAG, "onStart: ")
								FileTransferHelper.getInstance().currentFileIndex = index
								transferFile.transferProgress = 0
								TransferFileNotification.notify(this@ReceiveFileService, index)
								val message = Message()
								message.what = TransferHelperHandler.UPDATE_UI
								transferHelperHandler!!.sendMessage(message)
							}

							override fun onProgress(progress: Int)
							{
								Logs.i(TAG, "onProgress: " + progress)
								transferFile.transferProgress = progress
								TransferFileNotification.notify(this@ReceiveFileService, index)
								val message = Message()
								message.what = TransferHelperHandler.UPDATE_UI
								transferHelperHandler!!.sendMessage(message)
							}

							override fun onFinish()
							{
								Logs.i(TAG, "onFinish: " + transferFile.fileName)
								transferFile.transferProgress = 100
								TransferFileNotification.done(this@ReceiveFileService, index, transferFile)
								val map = HashMap<String, Any>()
								map.put("context", this@ReceiveFileService)
								map.put("fileName", transferFile.fileName!!)
								val message = Message()
								message.obj = map
								message.what = TransferHelperHandler.UPDATE_TOAST
								transferHelperHandler!!.sendMessage(message)
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
						Logs.i(TAG, "onStartCommand: 传输完成")
						FileTransferHelper.getInstance().fileList
								.filter { it.transferProgress == 100 }
								.forEachIndexed { index, transferFile ->
									TransferFileNotification.done(this@ReceiveFileService, index, transferFile)
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
				Logs.e(TAG, "onCreate: 获取请求头失败")
				stopSelf()
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
