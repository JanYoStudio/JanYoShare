package com.janyo.janyoshare.util

class FileTransferHandler private constructor()
{
	var host: String? = null
	var port: Int = 0
	var socketUtil: SocketUtil? = null

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