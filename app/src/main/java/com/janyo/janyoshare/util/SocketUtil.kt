package com.janyo.janyoshare.util

import com.mystery0.tools.Logs.Logs
import java.io.*
import java.net.ConnectException
import java.net.ServerSocket
import java.net.Socket


class SocketUtil
{
	private val TAG = "SocketUtil"
	var isServerClose = false
	lateinit var socket: Socket
	lateinit var serverSocket: ServerSocket
	lateinit var ip: String

	fun tryCreateSocketConnection(host: String, port: Int): Boolean
	{
		try
		{
			val socket = Socket(host, port)
			if (socket.isConnected)
			{
				socket.keepAlive = true
				ip = host
				this.socket = socket
			}
			return socket.isConnected
		}
		catch (e: ConnectException)
		{
			return false
		}
	}

	fun createSocketConnection(host: String, port: Int): Boolean
	{
		for (i in 0..19)
		{
			try
			{
				val socket = Socket(host, port)
				if (socket.isConnected)
				{
					this.socket = socket
					break
				}
			}
			catch (e: Exception)
			{
				Thread.sleep(100)
				continue
			}
		}
		socket.keepAlive = true
		ip = host
		return socket.isConnected
	}

	fun createServerConnection(port: Int): Boolean
	{
		try
		{
			serverSocket = ServerSocket(port)
			socket = serverSocket.accept()
			if (isServerClose)
				return false
			socket.keepAlive = true
			this.socket = socket
		}
		catch (e: Exception)
		{
			Logs.wtf(TAG, "createServerConnection", e)
			return false
		}
		return socket.isConnected
	}

	fun receiveMessage(): String
	{
		var response = "null"
		try
		{
			val inputStream = socket.getInputStream()
			response = BufferedReader(InputStreamReader(inputStream)).readLine()
		}
		catch (e: Exception)
		{
			Logs.wtf(TAG, "receiveMessage", e)
		}
		return response
	}

	fun sendMessage(message: String)
	{
		try
		{
			val outputStream = socket.getOutputStream()
			outputStream.write((message + "\n").toByteArray())
			outputStream.flush()
		}
		catch (e: Exception)
		{
			Logs.wtf(TAG, "sendMessage", e)
		}
	}

	fun receiveFile(fileSize: Long, path: String, host: String,
					fileTransferListener: FileTransferListener): File?
	{
		val file = File(path)
		var socket: Socket? = null
		if (file.exists())
		{
			fileTransferListener.onError(1, RuntimeException("file is exists!"))
			return null
		}
		try
		{
			Logs.i(TAG, "receiveFile: 文件大小" + fileSize)
			for (i in 0..19)
			{
				try
				{
					socket = Socket(host, FileTransferHandler.getInstance().transferPort)
					if (socket.isConnected)
					{
						break
					}
				}
				catch (e: Exception)
				{
					Thread.sleep(100)
					continue
				}
				if (i == 19)
					return null
			}
			val dataInputStream: DataInputStream? = DataInputStream(BufferedInputStream(socket!!.getInputStream()))
			val bytes = ByteArray(1024 * 1024)
			var transferredSize = 0L
			val dataOutputStream = DataOutputStream(BufferedOutputStream(FileOutputStream(file)))
			fileTransferListener.onStart()
			while (true)
			{
				val read = dataInputStream!!.read(bytes)
				transferredSize += read
				Logs.i(TAG, "receiveFile: " + read)
				if (read == -1)
				{
					Logs.i(TAG, "receiveFile: 退出循环")
					break
				}
				fileTransferListener.onProgress((transferredSize * 100 / fileSize).toInt())
				dataOutputStream.write(bytes, 0, read)
				Logs.i(TAG, "receiveFile: 写入" + read + "个数据")
				Thread.sleep(100)
			}
		}
		catch (e: Exception)
		{
			fileTransferListener.onError(2, e)
		}
		Logs.i(TAG, "receiveFile: 文件完成")
		socket!!.getOutputStream().close()
		socket.getInputStream().close()
		socket.close()
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
			Logs.i(TAG, "sendFile: fileSize" + file.length())
			val socket = ServerSocket(FileTransferHandler.getInstance().transferPort).accept()
			val dataOutputStream = DataOutputStream(socket.getOutputStream())
			val dataInputStream = DataInputStream(BufferedInputStream(FileInputStream(file)))
			var transferredSize = 0L
			val bytes = ByteArray(1024 * 1024)
			fileTransferListener.onStart()
			while (true)
			{
				val read = dataInputStream.read(bytes)
				Logs.i(TAG, "sendFile: " + read)
				transferredSize += read
				fileTransferListener.onProgress((transferredSize * 100 / file.length()).toInt())
				if (read <= 0)
				{
					Logs.i(TAG, "sendFile: 退出循环")
					break
				}
				dataOutputStream.write(bytes, 0, read)
			}
			dataOutputStream.flush()
			Logs.i(TAG, "sendFile: 推流")
			socket.getOutputStream().close()
			socket.getInputStream().close()
			socket.close()
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
			val objectInputStream = ObjectInputStream(BufferedInputStream(socket.getInputStream()))
			obj = objectInputStream.readObject()
		}
		catch (e: Exception)
		{
			Logs.wtf(TAG, "receiveObject", e)
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
			val objectOutputStream = ObjectOutputStream(socket.getOutputStream())
			objectOutputStream.writeObject(obj)
			objectOutputStream.flush()
		}
		catch (e: Exception)
		{
			Logs.wtf(TAG, "sendObject", e)
			return false
		}
		return true
	}

	fun clientDisconnect()
	{
		Logs.i(TAG, "clientDisconnect: 客户端终止连接")
		socket.close()
	}

	fun serverDisconnect()
	{
		Logs.i(TAG, "serverDisconnect: 服务端终止连接")
		isServerClose = true
		serverSocket.close()
	}

	interface FileTransferListener
	{
		fun onStart()
		fun onProgress(progress: Int)
		fun onFinish()
		fun onError(code: Int, e: Exception)
	}
}