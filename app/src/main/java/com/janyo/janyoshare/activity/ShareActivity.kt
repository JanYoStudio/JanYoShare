package com.janyo.janyoshare.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.util.FileTransferHelper
import com.mystery0.tools.Logs.Logs
import java.io.File

class ShareActivity : AppCompatActivity()
{
	private val TAG = "ShareActivity"
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = intent.action
		val type = intent.type
		if (action == Intent.ACTION_SEND && type != null)
		{
			try
			{
				val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
				Logs.i(TAG, "onCreate: " + uri)
				val transferFile = TransferFile()
				transferFile.fileUri = uri.toString()
				val file = File(uri.path)
				transferFile.fileName = file.name
				transferFile.fileSize = file.length()
				FileTransferHelper.getInstance().fileList.add(transferFile)
				val intent = Intent(this, FileTransferConfigureActivity::class.java)
				intent.putExtra("action", 1)
				startActivity(intent)
			}
			catch (e: Exception)
			{
				Logs.wtf(TAG, "onCreate: ", e)
			}
		}
		else
		{
			Toast.makeText(this, "无效的文件！", Toast.LENGTH_SHORT)
					.show()
		}
		finish()
	}
}