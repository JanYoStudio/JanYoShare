package com.janyo.janyoshare.util

import com.mystery0.tools.Logs.Logs
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class WifiUtil(private var host: String, private var port: Int)
{
	private val TAG = "WifiUtil"
	private var socket: Socket? = null
	private var out: DataOutputStream? = null
	private var getMessageStream: DataInputStream? = null

	fun createConnection()
	{
		socket = Socket(host, port)
		Logs.i(TAG, "createConnection: "+socket!!.isConnected)
	}

	fun sendMessage(message: String)
	{
		out = DataOutputStream(socket!!.getOutputStream())
		out!!.writeUTF(message)
		out!!.flush()
	}

	fun getMessageStream(): DataInputStream
	{
		getMessageStream = DataInputStream(BufferedInputStream(socket!!.getInputStream()))
		return getMessageStream!!
	}

	fun shutDownConnection()
	{
		if (out != null)
		{
			out!!.close()
		}
		if (getMessageStream != null)
		{
			getMessageStream!!.close()
		}
		if (socket != null)
		{
			socket!!.close()
		}
	}
}