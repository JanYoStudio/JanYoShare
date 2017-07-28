package com.janyo.janyoshare.handler

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.service.ReceiveFileService
import com.janyo.janyoshare.service.SendFileService

class ErrorHandler(private var context: Context) : Handler()
{
	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			UNKNOWN_ERROR -> Toast.makeText(context, R.string.hint_transfer_file_unknown_error, Toast.LENGTH_SHORT)
					.show()
			FILE_EXISTS -> Toast.makeText(context, R.string.hint_transfer_file_exists, Toast.LENGTH_SHORT)
					.show()
			FILE_NOT_EXISTS -> Toast.makeText(context, R.string.hint_transfer_file_not_exists, Toast.LENGTH_SHORT)
					.show()
		}
		val sendIntent = Intent(context, SendFileService::class.java)
		val receiveIntent = Intent(context, ReceiveFileService::class.java)
		context.stopService(sendIntent)
		context.stopService(receiveIntent)
	}

	companion object
	{
		val UNKNOWN_ERROR = 0
		val FILE_EXISTS = 1
		val FILE_NOT_EXISTS = 2
	}
}