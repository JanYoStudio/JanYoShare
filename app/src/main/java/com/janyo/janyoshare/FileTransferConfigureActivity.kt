@file:Suppress("DEPRECATION")

package com.janyo.janyoshare

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import com.janyo.janyoshare.util.SocketUtil
import com.janyo.janyoshare.util.WIFIUtilg
import com.mystery0.tools.Logs.Logs

import kotlinx.android.synthetic.main.activity_file_transfer_configure.*
import kotlinx.android.synthetic.main.content_file_transfer_configure.*

class FileTransferConfigureActivity : AppCompatActivity()
{
	private val TAG = "FileTransferConfigureActivity"
	private val sendHandler = SendHandler()
	private val receiveHandler = ReceiveHandler()
	private val socketUtil = SocketUtil()
	private var progressDialog:ProgressDialog?=null

	companion object
	{
		val CREATE_SERVER = 1
		val CREATE_CONNECTION = 2
		val SEND_MESSAGE = 3
		val RECEIVE_MESSAGE = 4
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_file_transfer_configure)
		setSupportActionBar(toolbar)

		progressDialog = ProgressDialog(this)
		progressDialog!!.setCancelable(false)
		sendHandler.setProgressDialog(progressDialog!!)
		receiveHandler.setProgressDialog(progressDialog!!)

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
				message_create.obj = socketUtil.createServerConnection(8989)
				sendHandler.sendMessage(message_create)
				val message_send = Message()
				message_send.what = SEND_MESSAGE
				socketUtil.sendMessage(getString(R.string.app_name))
				sendHandler.sendMessage(message_send)
			}).start()
		}

		receiveFile.setOnClickListener {
			progressDialog!!.setMessage("Waiting……")
			progressDialog!!.show()
			Thread(Runnable {
				WIFIUtilg(this).scanIP(object : WIFIUtilg.ScanListener
				{
					override fun onScanFinish(ipv4: String)
					{
						val message = Message()
						message.what = CREATE_CONNECTION
						message.obj = ipv4
						receiveHandler.sendMessage(message)
					}

					override fun onConnect()
					{
						val message = Message()
						message.what = RECEIVE_MESSAGE
						receiveHandler.sendMessage(message)
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
	private var progressDialog: ProgressDialog? = null

	fun setProgressDialog(progressDialog: ProgressDialog)
	{
		this.progressDialog = progressDialog
	}

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			FileTransferConfigureActivity.CREATE_SERVER ->
			{
				progressDialog!!.setMessage("Connecting " + msg.obj)
			}
			FileTransferConfigureActivity.SEND_MESSAGE ->
			{
				Logs.i(TAG, "handleMessage: 连接成功")
				progressDialog!!.dismiss()
			}
		}
	}
}

internal class ReceiveHandler : Handler()
{
	private val TAG = "ReceiveHandler"
	private var progressDialog: ProgressDialog? = null

	fun setProgressDialog(progressDialog: ProgressDialog)
	{
		this.progressDialog = progressDialog
	}

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			FileTransferConfigureActivity.CREATE_CONNECTION ->
			{
				progressDialog!!.setMessage("Connecting……")
			}
			FileTransferConfigureActivity.RECEIVE_MESSAGE ->
			{
				Logs.i(TAG, "handleMessage: 连接成功")
				progressDialog!!.dismiss()
			}
		}
	}
}
