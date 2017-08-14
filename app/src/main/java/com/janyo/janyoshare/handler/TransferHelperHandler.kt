package com.janyo.janyoshare.handler

import android.os.Handler
import android.os.Message
import com.janyo.janyoshare.adapter.FileTransferAdapter

class TransferHelperHandler : Handler()
{
	lateinit var adapter: FileTransferAdapter

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			UPDATE_UI -> adapter.notifyDataSetChanged()
		}
	}

	companion object
	{
		val UPDATE_UI = 1
	}
}