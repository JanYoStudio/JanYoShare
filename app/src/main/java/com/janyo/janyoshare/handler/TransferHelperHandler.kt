package com.janyo.janyoshare.handler

import android.content.Context
import android.os.Handler
import android.os.Message
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.adapter.FileTransferAdapter
import com.janyo.janyoshare.classes.TransferFile

class TransferHelperHandler : Handler()
{
	lateinit var adapter: FileTransferAdapter
	var list = ArrayList<TransferFile>()

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			UPDATE_UI ->
			{
				adapter.notifyDataSetChanged()
			}
			UPDATE_TOAST ->
			{
				@Suppress("UNCHECKED_CAST")
				val map = msg.obj as HashMap<String, Any>
				val context = map["context"] as Context
				Toast.makeText(context, context.getString(R.string.hint_transfer_file_notification_done, map["fileName"].toString()), Toast.LENGTH_SHORT)
						.show()
				adapter.notifyDataSetChanged()
			}
		}
	}

	companion object
	{
		val UPDATE_UI = 1
		val UPDATE_TOAST = 2
	}
}