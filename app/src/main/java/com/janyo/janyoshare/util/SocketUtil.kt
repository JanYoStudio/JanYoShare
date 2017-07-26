package com.janyo.janyoshare.util

import com.mystery0.tools.Logs.Logs
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class SocketUtil
{
	private val TAG = "SocketUtil"
	var socket: Socket? = null

	fun createSocketConnection(host: String, port: Int): Boolean
	{
		try
		{
			if (socket != null && socket!!.isConnected)
			{
				return true
			}
			socket = Socket(host, port)
			socket!!.keepAlive = true
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
		return socket!!.isConnected
	}

	fun createServerConnection(port: Int): Boolean
	{
		try
		{
			if (socket != null && socket!!.isConnected)
			{
				return true
			}
			socket = ServerSocket(port).accept()
			socket!!.keepAlive = true
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
		return socket!!.isConnected
	}

	fun receiveMessage(): String
	{
		var response = "null"
		try
		{
			val inputStream = socket!!.getInputStream()
			response = BufferedReader(InputStreamReader(inputStream)).readLine()
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
		return response
	}

	fun sendMessage(message: String)
	{
		try
		{
			val outputStream = socket!!.getOutputStream()
			outputStream.write((message + "\n").toByteArray())
			outputStream.flush()
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	fun receiveFile(fileSize: Long, path: String, fileTransferListener: FileTransferListener): File?
	{
		val file = File(path)
		if (file.exists())
		{
			fileTransferListener.onError(1, RuntimeException("file is exists!"))
			return null
		}
		try
		{
			file.createNewFile()
			val dataInputStream = DataInputStream(BufferedInputStream(socket!!.getInputStream()))
			val dataOutputStream = DataOutputStream(BufferedOutputStream(FileOutputStream(file)))
			val buffer = ByteArray(1024)
			var index = 0
			var transferredSize = 0L
			fileTransferListener.onStart()
			while (true)
			{
				val bytesRead = dataInputStream.read(buffer)
				transferredSize += bytesRead
				if (bytesRead <= 0)
				{
					break
				}
				index++
				if (index > 20)
				{
					val progress = (transferredSize * 100 / fileSize).toInt()
					fileTransferListener.onProgress(progress)
					index = 0
				}
				dataOutputStream.write(buffer, 0, bytesRead)
			}
			dataInputStream.close()
			dataOutputStream.close()
			fileTransferListener.onFinish()
		}
		catch (e: Exception)
		{
			fileTransferListener.onError(2, e)
		}
		return file
	}

	fun sendFile(file: File, fileTransferListener: FileTransferListener)
	{
		if (!file.exists())
		{
			fileTransferListener.onError(1, RuntimeException("file not exists!"))
			return
		}
		try
		{
			val dataOutputStream = DataOutputStream(socket!!.getOutputStream())
			val dataInputStream = DataInputStream(BufferedInputStream(FileInputStream(file)))
			val buffer = ByteArray(1024)
			var index = 0
			val fileSize = file.length()
			var transferredSize = 0L
			fileTransferListener.onStart()//文件传输准备完毕
			while (true)
			{
				val bytesRead = dataInputStream.read(buffer)
				transferredSize += bytesRead
				if (bytesRead <= 0)
				{
					break
				}
				index++
				if (index > 20)
				{
					val progress = (transferredSize * 100 / fileSize).toInt()
					fileTransferListener.onProgress(progress)
					index = 0
				}
				dataOutputStream.write(buffer, 0, bytesRead)
			}
			dataOutputStream.flush()
			dataOutputStream.close()
			dataInputStream.close()
			fileTransferListener.onFinish()
		}
		catch (e: Exception)
		{
			fileTransferListener.onError(2, e)
		}
	}

	fun receiveObject(): Any?
	{
		var obj: Any? = null
		try
		{
			val objectInputStream = ObjectInputStream(BufferedInputStream(socket!!.getInputStream()))
			obj = objectInputStream.readObject()
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
		return obj
	}

	fun sendObject(obj: Any?): Boolean
	{
		if (obj == null)
		{
			return false
		}
		try
		{
			val objectOutputStream = ObjectOutputStream(socket!!.getOutputStream())
			objectOutputStream.writeObject(obj)
			objectOutputStream.flush()
			objectOutputStream.close()
		}
		catch (e: Exception)
		{
			e.printStackTrace()
			return false
		}
		return true
	}

	fun disConnect()
	{
		Logs.i(TAG, "disConnect: 断开Socket连接")
		if (socket != null)
		{
			socket!!.close()
		}
	}

	interface FileTransferListener
	{
		fun onStart()
		fun onProgress(progress: Int)
		fun onFinish()
		fun onError(code: Int, e: Exception)
	}
}