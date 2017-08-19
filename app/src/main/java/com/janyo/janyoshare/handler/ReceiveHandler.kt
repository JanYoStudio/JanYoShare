@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.handler

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.activity.FileTransferActivity
import com.janyo.janyoshare.activity.FileTransferConfigureActivity
import com.janyo.janyoshare.service.ReceiveFileService
import com.janyo.janyoshare.util.FileTransferHelper
import com.janyo.janyoshare.util.SocketUtil

class ReceiveHandler : Handler()
{
	lateinit var progressDialog: ProgressDialog
	lateinit var context: Context

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			FileTransferConfigureActivity.CREATE_CONNECTION ->
			{
				progressDialog.setMessage(context.getString(R.string.hint_socket_connecting))
			}
			FileTransferConfigureActivity.VERIFY_DEVICE ->
			{
				@Suppress("UNCHECKED_CAST")
				val map = msg.obj as HashMap<String, Any>
				val socketUtil = map["socket"] as SocketUtil
				AlertDialog.Builder(context)
						.setCancelable(false)
						.setTitle(R.string.hint_socket_verify_device_title)
						.setMessage(String.format(context.getString(R.string.hint_socket_verify_device_message), map["message"]))
						.setPositiveButton(R.string.action_done, { _, _ ->
							Thread(Runnable {
								socketUtil.sendMessage(FileTransferConfigureActivity.VERIFY_DONE)

								val message = Message()
								message.what = FileTransferConfigureActivity.CONNECTED
								message.obj = map["message"]
								sendMessage(message)
								FileTransferHelper.getInstance().ip = socketUtil.ip
								socketUtil.clientDisconnect()
							}).start()
						})
						.setNegativeButton(R.string.action_cancel, { _, _ ->
							Thread(Runnable {
								socketUtil.sendMessage(FileTransferConfigureActivity.VERIFY_CANCEL)
							}).start()
						})
						.show()
			}
			FileTransferConfigureActivity.CONNECTED ->
			{
				progressDialog.dismiss()
				Toast.makeText(context, R.string.hint_socket_connected, Toast.LENGTH_SHORT)
						.show()
				FileTransferHelper.getInstance().tag = 2
				(context as Activity).finish()
				context.startActivity(Intent(context, FileTransferActivity::class.java))
				context.startService(Intent(context, ReceiveFileService::class.java))
			}
			FileTransferConfigureActivity.SCAN_COMPLETE ->
			{
				progressDialog.dismiss()
				val isDeviceFind = msg.obj as Boolean
				if (!isDeviceFind)
					Toast.makeText(context, R.string.hint_socket_scan_complete, Toast.LENGTH_SHORT)
							.show()
			}
			FileTransferConfigureActivity.VERIFY_ERROR ->
			{
				progressDialog.dismiss()
				Toast.makeText(context, R.string.hint_socket_verify_error, Toast.LENGTH_SHORT)
						.show()
			}
		}
	}
}