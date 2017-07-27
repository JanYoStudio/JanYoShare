@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.service.ReceiveFileService
import com.janyo.janyoshare.util.FileTransferHandler
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

	private val openAPThread = Thread(Runnable {
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
			FileTransferHandler.getInstance().socketUtil = socketUtil
			val message = Message.obtain()
			message.what = CONNECTED
			sendHandler.sendMessage(message)
		}
		else
		{
			Logs.e(TAG, "openServer: 连接错误")
			progressDialog!!.dismiss()
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
		progressDialog!!.setCancelable(false)
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
			progressDialog!!.setMessage(getString(R.string.hint_socket_wait_server))
			progressDialog!!.show()
			openAPThread.start()
			val transferFile = intent.getBundleExtra("app").getSerializable("app") as TransferFile
			Logs.i(TAG, "onCreate: " + transferFile.fileName)
			FileTransferHandler.getInstance().fileList.add(transferFile)
		}

		sendFile.setOnClickListener {
			progressDialog!!.setMessage(getString(R.string.hint_socket_wait_server))
			progressDialog!!.show()
			openAPThread.start()
		}

		receiveFile.setOnClickListener {
			progressDialog!!.setMessage(getString(R.string.hint_socket_wait_client))
			progressDialog!!.show()
			Thread(Runnable {
				WIFIUtil(this, PORT).scanIP(object : WIFIUtil.ScanListener
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
}

internal class SendHandler : Handler()
{
	var progressDialog: ProgressDialog? = null
	var context: Context? = null

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			FileTransferConfigureActivity.CREATE_SERVER ->
			{
				progressDialog!!.setMessage(context!!.getString(R.string.hint_socket_connecting))
			}
			FileTransferConfigureActivity.VERIFY_DEVICE ->
			{
				progressDialog!!.setMessage(context!!.getString(R.string.hint_socket_verifying))
			}
			FileTransferConfigureActivity.CONNECTED ->
			{
				progressDialog!!.dismiss()
				Toast.makeText(context, R.string.hint_socket_connected, Toast.LENGTH_SHORT)
						.show()
				FileTransferHandler.getInstance().tag = 1
				context!!.startActivity(Intent(context, FileTransferActivity::class.java))
			}
		}
	}
}

internal class ReceiveHandler : Handler()
{
	var progressDialog: ProgressDialog? = null
	var context: Context? = null

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			FileTransferConfigureActivity.CREATE_CONNECTION ->
			{
				progressDialog!!.setMessage(context!!.getString(R.string.hint_socket_connecting))
			}
			FileTransferConfigureActivity.VERIFY_DEVICE ->
			{
				@Suppress("UNCHECKED_CAST")
				val map = msg.obj as HashMap<String, Any>
				AlertDialog.Builder(context!!)
						.setTitle(R.string.hint_socket_verify_device_title)
						.setMessage(String.format(context!!.getString(R.string.hint_socket_verify_device_message), map["message"]))
						.setPositiveButton(R.string.action_done, { _, _ ->
							Thread(Runnable {
								val socketUtil = map["socket"] as SocketUtil
								FileTransferHandler.getInstance().socketUtil = socketUtil

								socketUtil.sendMessage(FileTransferConfigureActivity.VERIFY_DONE)

								val message = Message()
								message.what = FileTransferConfigureActivity.CONNECTED
								message.obj = map["message"]
								sendMessage(message)
							}).start()
						})
						.setNegativeButton(R.string.action_cancel, null)
						.show()
			}
			FileTransferConfigureActivity.CONNECTED ->
			{
				progressDialog!!.dismiss()
				Toast.makeText(context, R.string.hint_socket_connected, Toast.LENGTH_SHORT)
						.show()
				FileTransferHandler.getInstance().tag = 2
				context!!.startService(Intent(context, ReceiveFileService::class.java))
				context!!.startActivity(Intent(context, FileTransferActivity::class.java))
			}
			FileTransferConfigureActivity.SCAN_COMPLETE ->
			{
				progressDialog!!.dismiss()
				Toast.makeText(context, R.string.hint_socket_scan_complete, Toast.LENGTH_SHORT)
						.show()
			}
		}
	}
}
