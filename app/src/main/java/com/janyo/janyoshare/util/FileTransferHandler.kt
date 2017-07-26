package com.janyo.janyoshare.util

import com.janyo.janyoshare.classes.TransferFile

class FileTransferHandler private constructor()
{
	var tag = 0//1表示发送文件，2表示接收文件
	var socketUtil: SocketUtil? = null
	var fileList = ArrayList<TransferFile>()
	var currentFile: TransferFile? = null
	var currentProgress = 0

	private object instance
	{
		val instance = FileTransferHandler()
	}

	companion object
	{
		fun getInstance(): FileTransferHandler
		{
			return instance.instance
		}
	}
}