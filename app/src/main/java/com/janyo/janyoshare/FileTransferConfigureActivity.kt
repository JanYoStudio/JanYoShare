@file:Suppress("DEPRECATION")

package com.janyo.janyoshare

import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.WIFIUtil
import com.mystery0.tools.Logs.Logs

import kotlinx.android.synthetic.main.activity_file_transfer_configure.*
import kotlinx.android.synthetic.main.content_file_transfer_configure.*

class FileTransferConfigureActivity : AppCompatActivity()
{
	private val TAG = "FileTransferConfigureActivity"
	private val PORT = 8989
	private val sendHandler = SendHandler()
	private val receiveHandler = ReceiveHandler()
	private var progressDialog: ProgressDialog? = null
	private val socketUtil = SocketUtil()

	companion object
	{
		val CREATE_SERVER = 1
		val CREATE_CONNECTION = 2
		val SEND_MESSAGE = 3
		val CONNECTED = 4
		val VERIFY_DEVICE = 5
		val VERIFY_DONE = "VERIFY_DONE"
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_file_transfer_configure)
		setSupportActionBar(toolbar)

		progressDialog = ProgressDialog(this)
		progressDialog!!.setCancelable(false)
		sendHandler.progressDialog = progressDialog
		receiveHandler.progressDialog = progressDialog
		receiveHandler.context = this

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
		{
			receiveFile.setBackgroundResource(R.drawable.ic_circle_indigo)
		}

		sendFile.setOnClickListener {
			progressDialog!!.setMessage("Waiting……")
			progressDialog!!.show()
			Thread(Runnable {
				val message_create = Message()
				message_create.what = CREATE_SERVER
				message_create.obj = socketUtil.createServerConnection(PORT)
				sendHandler.sendMessage(message_create)
				val message_send = Message()
				message_send.what = VERIFY_DEVICE
				socketUtil.sendMessage(Build.MODEL)
				sendHandler.sendMessage(message_send)
				if (socketUtil.receiveMessage() == VERIFY_DONE)
				{
					Logs.i(TAG, "onCreate: 开始传输")
					progressDialog!!.dismiss()
				}
			}).start()
		}

		receiveFile.setOnClickListener {
			progressDialog!!.setMessage("Waiting……")
			progressDialog!!.show()
			Thread(Runnable {
				WIFIUtil(this, PORT).scanIP(object : WIFIUtil.ScanListener
				{
					override fun onScanFinish(ipv4: String, socketUtil: SocketUtil)
					{
						val message = Message()
						message.what = CREATE_CONNECTION
						message.obj = ipv4
						receiveHandler.sendMessage(message)

						val resultMessage = socketUtil.receiveMessage()
						val message_verify = Message()
						message_verify.what = VERIFY_DEVICE
						val map = HashMap<String, Any>()
						map.put("message", resultMessage)
						map.put("socket", socketUtil)
						message_verify.obj = map
						receiveHandler.sendMessage(message_verify)
					}

					override fun onNoting()
					{
						Logs.i(TAG, "onNoting: ")
					}

				})
			}).start()
		}
	}
}

internal class SendHandler : Handler()
{
	private val TAG = "SendHandler"
	var progressDialog: ProgressDialog? = null

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			FileTransferConfigureActivity.CREATE_SERVER ->
			{
				Logs.i(TAG, "Connecting……")
				progressDialog!!.setMessage("Connecting……")
			}
			FileTransferConfigureActivity.SEND_MESSAGE ->
			{
				Logs.i(TAG, "handleMessage: 连接成功")
				progressDialog!!.dismiss()
			}
			FileTransferConfigureActivity.VERIFY_DEVICE ->
			{
				Logs.i(TAG, "handleMessage: 验证设备")
			}
		}
	}
}

internal class ReceiveHandler : Handler()
{
	private val TAG = "ReceiveHandler"
	var progressDialog: ProgressDialog? = null
	var context: Context? = null

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			FileTransferConfigureActivity.CREATE_CONNECTION ->
			{
				Logs.i(TAG, "Connecting " + msg.obj)
				progressDialog!!.setMessage("Connecting……")
			}
			FileTransferConfigureActivity.CONNECTED ->
			{
				Logs.i(TAG, "handleMessage: 连接成功" + msg.obj)
				progressDialog!!.dismiss()
			}
			FileTransferConfigureActivity.VERIFY_DEVICE ->
			{
				@Suppress("UNCHECKED_CAST")
				val map = msg.obj as HashMap<String, Any>
				AlertDialog.Builder(context!!)
						.setTitle("确认设备？")
						.setMessage("确认将文件发送给 " + map["message"] + " 吗？")
						.setPositiveButton("确定", { _, _ ->
							Thread(Runnable {
								val socketUtil = map["socket"] as SocketUtil
								socketUtil.sendMessage(FileTransferConfigureActivity.VERIFY_DONE)
								val message = Message()
								message.what = FileTransferConfigureActivity.CONNECTED
								message.obj = map["message"]
								sendMessage(message)
							}).start()
						})
						.setNegativeButton("取消", null)
						.show()
			}
		}
	}
}
