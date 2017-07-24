package com.janyo.janyoshare.util

import com.mystery0.tools.Logs.Logs
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class SocketUtil
{
	private val TAG = "SocketUtil"
	var host: String = ""
	var port: Int = 0
	var socket: Socket? = null

	fun createSocketConnection(): Boolean
	{
		return createSocketConnection(host, port)
	}

	fun createServerConnection(): Boolean
	{
		return createServerConnection(port)
	}

	fun createSocketConnection(host: String, port: Int): Boolean
	{
		socket = Socket(host, port)
		return socket!!.isConnected
	}

	fun createServerConnection(port: Int): Boolean
	{
		socket = ServerSocket(port).accept()
		return socket!!.isConnected
	}

	fun receiveMessage(): String
	{
		val inputStream = socket!!.getInputStream()
		val response = BufferedReader(InputStreamReader(inputStream)).readLine()
		return response
	}

	fun sendMessage(message: String)
	{
		val outStream = socket!!.getOutputStream()
		outStream.write((message + "\n").toByteArray())
		outStream.flush()
	}

	fun disConnect()
	{
		socket!!.close()
	}
}