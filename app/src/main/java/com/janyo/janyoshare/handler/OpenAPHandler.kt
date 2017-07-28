package com.janyo.janyoshare.handler

import android.os.Handler
import android.os.Message
import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter
import com.janyo.janyoshare.classes.TransferFile


class OpenAPHandler : Handler()
{
	override fun handleMessage(msg: Message)
	{
		@Suppress("UNCHECKED_CAST")
		val map = msg.obj as HashMap<String, Any>
		val adapter = map["this"] as AppRecyclerViewAdapter
		val transferFile = map["transferFile"] as TransferFile
		adapter.openAP(transferFile)
	}
}