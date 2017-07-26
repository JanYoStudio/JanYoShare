package com.janyo.janyoshare.util

import com.mystery0.tools.Logs.Logs
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class SocketUtil
{
	private val TAG = "SocketUtil"
	var host: String = ""
	var port: Int = 0
	var socket: Socket? = null

	fun createSocketConnection(host: String, port: Int): Boolean
	{
		this.host = host
		this.port = port
		try
		{
			socket = Socket(host, port)
		}
		catch (e: Exception)
		{
			e.printStackTrace()
			if (socket != null)
				socket!!.close()
		}
		return socket!!.isConnected
	}

	fun createServerConnection(port: Int): Boolean
	{
		this.port = port
		try
		{
			socket = ServerSocket(port).accept()
		}
		catch (e: Exception)
		{
			e.printStackTrace()
			if (socket != null)
				socket!!.close()
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
			fileTransferListener.onError(1, RuntimeException("file not exists!"))
			return null
		}
		try
		{
			file.createNewFile()
			val socketInputStream = socket!!.getInputStream()
			val fileOutputStream = FileOutputStream(file)
			val buffer = ByteArray(1024)
			var index = 0
			fileTransferListener.onStart()
			var bytesRead = socketInputStream.read(buffer)
			while (bytesRead > 0)
			{
				Logs.i(TAG, "sendFile: " + bytesRead)
				fileOutputStream.write(buffer, 0, bytesRead)
				bytesRead = socketInputStream.read(buffer)
				index++
				if (index > 20)
				{
					val progress = (fileOutputStream.channel.size() / fileSize).toInt()
					fileTransferListener.onProgress(progress)
				}
			}
			socketInputStream.close()
			fileOutputStream.close()
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
			val socketOutputStream = socket!!.getOutputStream()
			val fileInputStream = FileInputStream(file)
			val buffer = ByteArray(1024)
			var index = 0
			val fileSize = fileInputStream.channel.size()
			fileTransferListener.onStart()//文件传输准备完毕
			var bytesRead = fileInputStream.read(buffer)
			while (bytesRead > 0)
			{
				Logs.i(TAG, "sendFile: " + bytesRead)
				socketOutputStream.write(buffer, 0, bytesRead)
				bytesRead = fileInputStream.read(buffer)
				index++
				if (index > 20)
				{
					val progress = ((fileSize - fileInputStream.channel.size()) / fileSize).toInt()
					fileTransferListener.onProgress(progress)
				}
			}
			socketOutputStream.close()
			fileInputStream.close()
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