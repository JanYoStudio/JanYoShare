package com.janyo.janyoshare

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mystery0.tools.Logs.Logs
import kotlinx.android.synthetic.main.activity_test.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset

class TestActivity : AppCompatActivity()
{
	private val TAG = "TestActivity"
	private var socket: Socket? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_test)


		createConnection.setOnClickListener {
			Thread(Runnable {
								socket = Socket(ip.text.toString(), 8989)
//				socket = ServerSocket(8989).accept()
				Logs.i(TAG, "onCreate: " + socket!!.isConnected)
			}).start()
		}

		getMessage.setOnClickListener {
			Thread(Runnable {
				val inputStream = socket!!.getInputStream()
				Logs.i(TAG, "onCreate: " + BufferedReader(InputStreamReader(inputStream)).readLine())
			}).start()
		}

		sendMessage.setOnClickListener {
			Thread(Runnable {
				val outputStream = socket!!.getOutputStream()
				outputStream.write((ip.text.toString() + "\n").toByteArray())
				outputStream.flush()
			}).start()
		}

		disconnect.setOnClickListener {
			Thread(Runnable {
				socket!!.close()
				Logs.i(TAG, "onCreate: " + socket!!.isConnected)
			}).start()
		}
	}
}
