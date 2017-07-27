@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.activity

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.support.v7.app.AppCompatActivity
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.handler.ReceiveHandler
import com.janyo.janyoshare.handler.SendHandler
import com.janyo.janyoshare.util.FileTransferHandler
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.WIFIUtil
import com.mystery0.tools.Logs.Logs

import kotlinx.android.synthetic.main.activity_file_transfer_configure.*
import kotlinx.android.synthetic.main.content_file_transfer_configure.*

class FileTransferConfigureActivity : AppCompatActivity()
{
	private val TAG = "FileTransferConfigureActivity"

	private val sendHandler = SendHandler()
	private val receiveHandler = ReceiveHandler()
	private lateinit var progressDialog: ProgressDialog
	private val socketUtil = SocketUtil()

	private val openAPThread = Thread(Runnable {
		val message_create = Message()
		message_create.what = CREATE_SERVER
		message_create.obj = socketUtil.createServerConnection(FileTransferHandler.getInstance().verifyPort)
		sendHandler.sendMessage(message_create)
		val message_send = Message()
		message_send.what = VERIFY_DEVICE
		socketUtil.sendMessage(Build.MODEL)
		sendHandler.sendMessage(message_send)
		if (socketUtil.receiveMessage() == VERIFY_DONE)
		{
			val message = Message.obtain()
			message.what = CONNECTED
			sendHandler.sendMessage(message)
			socketUtil.disConnect()
		}
		else
		{
			Logs.e(TAG, "openServer: 连接错误")
			progressDialog.dismiss()
		}
	})

	companion object
	{
		val CREATE_SERVER = 1
		val CREATE_CONNECTION = 2
		val CONNECTED = 3
		val VERIFY_DEVICE = 4
		val SCAN_COMPLETE = 5
		val VERIFY_DONE = "VERIFY_DONE"
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_file_transfer_configure)
		setSupportActionBar(toolbar)

		progressDialog = ProgressDialog(this)
		progressDialog.setCancelable(false)
		sendHandler.progressDialog = progressDialog
		sendHandler.context = this
		receiveHandler.progressDialog = progressDialog
		receiveHandler.context = this

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
		{
			receiveFile.setBackgroundResource(R.drawable.ic_circle_indigo)
		}

		if (intent.getIntExtra("action", 0) == 1)
		{
			progressDialog.setMessage(getString(R.string.hint_socket_wait_server))
			progressDialog.show()
			openAPThread.start()
			val transferFile = intent.getBundleExtra("app").getSerializable("app") as TransferFile
			Logs.i(TAG, "onCreate: " + transferFile.fileName)
			FileTransferHandler.getInstance().fileList.add(transferFile)
		}

		sendFile.setOnClickListener {
			progressDialog.setMessage(getString(R.string.hint_socket_wait_server))
			progressDialog.show()
			openAPThread.start()
		}

		receiveFile.setOnClickListener {
			progressDialog.setMessage(getString(R.string.hint_socket_wait_client))
			progressDialog.show()
			Thread(Runnable {
				WIFIUtil(this, FileTransferHandler.getInstance().verifyPort).scanIP(object : WIFIUtil.ScanListener
				{
					override fun onScan(ipv4: String, socketUtil: SocketUtil)
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

					override fun onError(e: Exception)
					{
					}

					override fun onFinish()
					{
						Logs.i(TAG, "onError: 搜索完毕")
						val message = Message()
						message.what = SCAN_COMPLETE
						receiveHandler.sendMessage(message)
					}
				})
			}).start()
		}
	}

	override fun onBackPressed()
	{
		if (openAPThread.isAlive)
		{
			openAPThread.interrupt()
			progressDialog.dismiss()
		}
		else
			super.onBackPressed()
	}
}

