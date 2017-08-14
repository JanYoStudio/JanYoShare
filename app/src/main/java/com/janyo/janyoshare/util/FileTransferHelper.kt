package com.janyo.janyoshare.util

import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.handler.TransferHelperHandler

class FileTransferHelper private constructor()
{
	var tag = 0//1表示发送文件，2表示接收文件
	var fileList = ArrayList<TransferFile>()
	var currentFileIndex = 0
	var ip: String = "null"
	var transferHelperHandler: TransferHelperHandler? = null
	val verifyPort = 8989
	val transferPort = 2333

	private object instance
	{
		val instance = FileTransferHelper()
	}

	fun clear()
	{
		tag = 0
		fileList.clear()
		currentFileIndex = 0
		ip = "null"
	}

	companion object
	{
		fun getInstance(): FileTransferHelper
		{
			return instance.instance
		}
	}
}