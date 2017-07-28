@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.handler

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.activity.FileTransferConfigureActivity
import com.janyo.janyoshare.service.ReceiveFileService
import com.janyo.janyoshare.util.FileTransferHandler
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
				AlertDialog.Builder(context)
						.setCancelable(false)
						.setTitle(R.string.hint_socket_verify_device_title)
						.setMessage(String.format(context.getString(R.string.hint_socket_verify_device_message), map["message"]))
						.setPositiveButton(R.string.action_done, { _, _ ->
							Thread(Runnable {
								val socketUtil = map["socket"] as SocketUtil

								socketUtil.sendMessage(FileTransferConfigureActivity.VERIFY_DONE)

								val message = Message()
								message.what = FileTransferConfigureActivity.CONNECTED
								message.obj = map["message"]
								sendMessage(message)
								FileTransferHandler.getInstance().ip = socketUtil.ip
								socketUtil.clientDisconnect()
							}).start()
						})
						.setNegativeButton(R.string.action_cancel, null)
						.show()
			}
			FileTransferConfigureActivity.CONNECTED ->
			{
				progressDialog.dismiss()
				Toast.makeText(context, R.string.hint_socket_connected, Toast.LENGTH_SHORT)
						.show()
				FileTransferHandler.getInstance().tag = 2
				val intent = Intent(context, ReceiveFileService::class.java)
//				intent.putExtra("action", "start")
				context.startService(intent)
//				context.startActivity(Intent(context, FileTransferActivity::class.java))
			}
			FileTransferConfigureActivity.SCAN_COMPLETE ->
			{
				progressDialog.dismiss()
				val isDeviceFind = msg.obj as Boolean
				if (!isDeviceFind)
					Toast.makeText(context, R.string.hint_socket_scan_complete, Toast.LENGTH_SHORT)
							.show()
			}
		}
	}
}