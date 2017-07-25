package com.janyo.janyoshare.util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class SocketUtil
{
	var host: String = ""
	var port: Int = 0
	var socket: Socket? = null

	fun createSocketConnection(host: String, port: Int): Boolean
	{
		this.host=host
		this.port=port
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
		this.port=port
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
		val response: String
		try
		{
			val inputStream = socket!!.getInputStream()
			response = BufferedReader(InputStreamReader(inputStream)).readLine()
		}
		catch (e: Exception)
		{
			e.printStackTrace()
			return "null"
		}
		return response
	}

	fun sendMessage(message: String)
	{
		try
		{
			val outStream = socket!!.getOutputStream()
			outStream.write((message + "\n").toByteArray())
			outStream.flush()
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	fun disConnect()
	{
		if (socket != null)
		{
			socket!!.close()
		}
	}
}