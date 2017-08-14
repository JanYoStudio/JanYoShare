package com.janyo.janyoshare.util

import com.janyo.janyoshare.classes.TransferFile

class FileTransferHandler private constructor()
{
	var tag = 0//1表示发送文件，2表示接收文件
	var fileList = ArrayList<TransferFile>()
	var currentFile: TransferFile? = null
	var currentProgress = 0
	var ip: String = "null"
	val verifyPort = 8989
	val transferPort = 2333

	private object instance
	{
		val instance = FileTransferHandler()
	}

	fun clear()
	{
		tag = 0
		fileList.clear()
		currentFile = null
		currentProgress = 0
		ip = "null"
	}

	companion object
	{
		fun getInstance(): FileTransferHandler
		{
			return instance.instance
		}
	}
}