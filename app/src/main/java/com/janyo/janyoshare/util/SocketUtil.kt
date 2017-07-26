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
			val dataInputStream: DataInputStream? = DataInputStream(BufferedInputStream(socket!!.getInputStream()))
			val bytes = ByteArray(1024)
			var transferredSize = 0L
			val dataOutputStream = DataOutputStream(BufferedOutputStream(BufferedOutputStream(FileOutputStream(file))))
			fileTransferListener.onStart()
			while (true)
			{
				val read = dataInputStream!!.read(bytes)
				transferredSize += read
				Logs.i(TAG, "receiveFile: " + read)
				if (read <= 0)
				{
					break
				}
				fileTransferListener.onProgress((transferredSize * 100 / fileSize).toInt())
				dataOutputStream.write(bytes, 0, read)
			}
		}
		catch (e: Exception)
		{
			fileTransferListener.onError(2, e)
		}
		fileTransferListener.onFinish()
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
			var transferredSize = 0L
			val bytes = ByteArray(1024)
			fileTransferListener.onStart()
			while (true)
			{
				val read = dataInputStream.read(bytes)
				Logs.i(TAG, "sendFile: " + read)
				transferredSize += read
				if (read <= 0)
				{
					break
				}
				fileTransferListener.onProgress((transferredSize * 100 / file.length()).toInt())
				dataOutputStream.write(bytes, 0, read)
			}
			dataOutputStream.flush()
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
		Logs.i(TAG, "disConnect: 断开Socket连接")
		if (socket != null)
		{
			socket!!.getInputStream().close()
			socket!!.getOutputStream().close()
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