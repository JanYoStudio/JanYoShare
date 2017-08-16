package com.janyo.janyoshare.handler

import android.os.Handler
import android.os.Message
import com.janyo.janyoshare.adapter.FileTransferAdapter
import com.janyo.janyoshare.classes.TransferFile

class TransferHelperHandler : Handler()
{
	lateinit var adapter: FileTransferAdapter
	var list=ArrayList<TransferFile>()

	override fun handleMessage(msg: Message)
	{
		when (msg.what)
		{
			UPDATE_UI ->
			{
				adapter.notifyDataSetChanged()
			}
		}
	}

	companion object
	{
		val UPDATE_UI = 1
	}
}