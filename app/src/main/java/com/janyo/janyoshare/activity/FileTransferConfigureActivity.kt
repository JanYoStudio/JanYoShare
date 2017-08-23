@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.activity

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.transition.TransitionInflater
import android.view.Window
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.handler.ReceiveHandler
import com.janyo.janyoshare.handler.SendHandler
import com.janyo.janyoshare.handler.TransferHelperHandler
import com.janyo.janyoshare.util.FileTransferHelper
import com.janyo.janyoshare.util.Settings
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.WIFIUtil
import com.mystery0.tools.Logs.Logs

import kotlinx.android.synthetic.main.content_file_transfer_configure.*
import java.util.concurrent.Executors

class FileTransferConfigureActivity : AppCompatActivity()
{
	private val TAG = "FileTransferConfigureActivity"
	private val sendHandler = SendHandler()
	private val receiveHandler = ReceiveHandler()
	private lateinit var progressDialog: ProgressDialog
	private val socketUtil = SocketUtil()
	private val singleThreadPool = Executors.newSingleThreadExecutor()

	companion object
	{
		val CREATE_SERVER = 1
		val CREATE_CONNECTION = 2
		val CONNECTED = 3
		val VERIFY_DEVICE = 4
		val SCAN_COMPLETE = 5
		val VERIFY_ERROR = 6
		val VERIFY_DONE = "VERIFY_DONE"
		val VERIFY_CANCEL = "VERIFY_CANCEL"
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		if (Settings.getInstance(this).dayNight)
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		else
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		super.onCreate(savedInstanceState)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
			window.exitTransition = TransitionInflater.from(this).inflateTransition(android.R.transition.slide_right)
			window.enterTransition = TransitionInflater.from(this).inflateTransition(android.R.transition.slide_left)
			window.reenterTransition = TransitionInflater.from(this).inflateTransition(android.R.transition.slide_left)
		}
		setContentView(R.layout.activity_file_transfer_configure)

		progressDialog = ProgressDialog(this)
		sendHandler.progressDialog = progressDialog
		sendHandler.context = this
		receiveHandler.progressDialog = progressDialog
		receiveHandler.context = this
		FileTransferHelper.getInstance().transferHelperHandler = TransferHelperHandler()

		if (intent.getIntExtra("action", 0) == 1)
		{
			FileTransferHelper.getInstance().transferHelperHandler!!.list.clear()
			FileTransferHelper.getInstance().transferHelperHandler!!.list.addAll(FileTransferHelper.getInstance().fileList)
			openAP()
		}

		sendFile.setOnClickListener {
			throw Exception("233")
			openAP()
		}

		receiveFile.setOnClickListener {
			progressDialog.setMessage(getString(R.string.hint_socket_wait_client))
			progressDialog.show()
			val task = singleThreadPool.submit {
				WIFIUtil(this, FileTransferHelper.getInstance().verifyPort).scanIP(object : WIFIUtil.ScanListener
				{
					override fun onScan(ipv4: String, socketUtil: SocketUtil)
					{
						val message = Message()
						message.what = CREATE_CONNECTION
						message.obj = ipv4
						receiveHandler.sendMessage(message)
						Thread.sleep(100)
						Logs.i(TAG, "openAP: 100阻塞线程")

						val resultMessage = socketUtil.receiveMessage()
						if (resultMessage == "null")
						{
							Logs.i(TAG, "onScan: 超时")
							val message_error = Message()
							message_error.what = VERIFY_ERROR
							receiveHandler.sendMessage(message_error)
							return
						}
						val message_verify = Message()
						message_verify.what = VERIFY_DEVICE
						val map = HashMap<String, Any>()
						map.put("message", resultMessage)
						map.put("socket", socketUtil)
						message_verify.obj = map
						receiveHandler.sendMessage(message_verify)
						Thread.sleep(100)
						Logs.i(TAG, "onScan: 100阻塞线程")
					}

					override fun onError(e: Exception)
					{
						Thread.sleep(100)
					}

					override fun onFinish(isDeviceFind: Boolean)
					{
						Logs.i(TAG, "onFinish: " + isDeviceFind)
						val message = Message()
						message.what = SCAN_COMPLETE
						message.obj = isDeviceFind
						receiveHandler.sendMessage(message)
						Thread.sleep(100)
						Logs.i(TAG, "onFinish: 100阻塞线程")
					}
				})
			}
			progressDialog.setOnCancelListener {
				Logs.i(TAG, "openAP: 监听到返回键")
				task.cancel(true)
			}
		}
	}

	private fun openAP()
	{
		progressDialog.setMessage(getString(R.string.hint_socket_wait_server))
		progressDialog.show()
		val task = singleThreadPool.submit {
			Logs.i(TAG, "openAP: 创建服务端")
			val message_create = Message()
			message_create.what = CREATE_SERVER
			if (!socketUtil.createServerConnection(FileTransferHelper.getInstance().verifyPort))
			{
				Thread.sleep(100)
				Logs.i(TAG, "openAP: 创建服务端失败")
				progressDialog.dismiss()
				Toast.makeText(this, R.string.hint_socket_timeout, Toast.LENGTH_SHORT)
						.show()
				return@submit
			}
			Thread.sleep(100)
			sendHandler.sendMessage(message_create)
			Logs.i(TAG, "openAP: 验证设备")
			val message_send = Message()
			message_send.what = VERIFY_DEVICE
			socketUtil.sendMessage(Build.MODEL)
			sendHandler.sendMessage(message_send)
			Thread.sleep(100)
			if (socketUtil.receiveMessage() == VERIFY_DONE)
			{
				Logs.i(TAG, "openAP: 验证完成")
				FileTransferHelper.getInstance().ip = socketUtil.socket.remoteSocketAddress.toString().substring(1)
				val message = Message.obtain()
				message.what = FileTransferConfigureActivity.CONNECTED
				sendHandler.sendMessage(message)
				Logs.i(TAG, "openAP: 断开验证连接")
				socketUtil.clientDisconnect()
				socketUtil.serverDisconnect()
			}
			else
			{
				Logs.e(TAG, "openServer: 连接错误")
				socketUtil.serverDisconnect()
				progressDialog.dismiss()
			}
			Thread.sleep(100)
		}
		progressDialog.setOnCancelListener {
			Logs.i(TAG, "openAP: 监听到返回键")
			task.cancel(true)
		}
	}

	override fun onDestroy()
	{
		super.onDestroy()
		singleThreadPool.shutdown()
	}
}

