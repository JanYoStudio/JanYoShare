@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.handler.ReceiveHandler
import com.janyo.janyoshare.handler.SendHandler
import com.janyo.janyoshare.util.FileTransferHandler
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.WIFIUtil
import com.mystery0.tools.FileUtil.FileUtil
import com.mystery0.tools.Logs.Logs

import kotlinx.android.synthetic.main.content_file_transfer_configure.*
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future

class FileTransferConfigureActivity : AppCompatActivity()
{
	private val TAG = "FileTransferConfigureActivity"
	private val CHOOSE_FILE = 233
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
		val VERIFY_DONE = "VERIFY_DONE"
		val VERIFY_CANCEL = "VERIFY_CANCEL"
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_file_transfer_configure)

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
//			val transferFile = intent.getBundleExtra("app").getSerializable("app") as TransferFile
//			Logs.i(TAG, "onCreate: " + transferFile.fileName)
//			Logs.i(TAG, "onCreate: " + transferFile.fileUri)
			openAP()
		}

		sendFile.setOnClickListener {
			//			if (FileTransferHandler.getInstance().fileList.size == 0)
//			{
//				val intent = Intent(Intent.ACTION_GET_CONTENT)
//				intent.type = "*/*"
//				intent.addCategory(Intent.CATEGORY_OPENABLE)
//				startActivityForResult(intent, CHOOSE_FILE)
//			}
//			else
//			{
			openAP()
//			}
		}

		receiveFile.setOnClickListener {
			progressDialog.setMessage(getString(R.string.hint_socket_wait_client))
			progressDialog.show()
			singleThreadPool.execute {
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

					override fun onFinish(isDeviceFind: Boolean)
					{
						Logs.i(TAG, "onFinish: " + isDeviceFind)
						val message = Message()
						message.what = SCAN_COMPLETE
						message.obj = isDeviceFind
						receiveHandler.sendMessage(message)
					}
				})
			}
		}
	}

//	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
//	{
//		if (requestCode == CHOOSE_FILE && data != null)
//		{
//			val file = File(FileUtil.getPath(this, data.data))
//			val transferFile = TransferFile()
//			transferFile.fileName = file.name
//			transferFile.fileUri = FileProvider.getUriForFile(this, getString(R.string.authorities), file).toString()
//			transferFile.fileSize = file.length()
//			progressDialog.setMessage(getString(R.string.hint_socket_wait_server))
//			progressDialog.show()
//			openAP()
//		}
//	}

	fun openAP()
	{
		progressDialog.setCancelable(true)
		progressDialog.setMessage(getString(R.string.hint_socket_wait_server))
		progressDialog.show()
		val task = singleThreadPool.submit(Runnable {
			Logs.i(TAG, "openAP: 创建服务端")
			val message_create = Message()
			message_create.what = FileTransferConfigureActivity.CREATE_SERVER
			if (!socketUtil.createServerConnection(FileTransferHandler.getInstance().verifyPort))
			{
				Logs.i(TAG, "openAP: 创建服务端失败")
				return@Runnable
			}
			sendHandler.sendMessage(message_create)
			Logs.i(TAG, "openAP: 验证设备")
			val message_send = Message()
			message_send.what = FileTransferConfigureActivity.VERIFY_DEVICE
			socketUtil.sendMessage(Build.MODEL)
			sendHandler.sendMessage(message_send)
			if (socketUtil.receiveMessage() == FileTransferConfigureActivity.VERIFY_DONE)
			{
				Logs.i(TAG, "openAP: 验证完成")
				FileTransferHandler.getInstance().ip = socketUtil.socket.remoteSocketAddress.toString().substring(1)
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
		})
		progressDialog.setOnCancelListener {
			Logs.i(TAG, "openAP: 监听到返回键")
			task.cancel(true)
		}
	}
}

